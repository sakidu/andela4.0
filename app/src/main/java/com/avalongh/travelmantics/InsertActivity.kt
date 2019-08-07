package com.avalongh.travelmantics

import android.app.Activity
import android.app.ListActivity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_insert.*
import kotlinx.android.synthetic.main.list_deals.view.*
import java.util.*
import android.widget.ImageView as ImageView1
import kotlinx.android.synthetic.main.activity_insert.deal_image_view as deal_image_view1

class InsertActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insert)

        val myuid = FirebaseAuth.getInstance().uid



        if(myuid ==null) {
            startActivity(Intent(this, com.avalongh.travelmantics.ListActivity::class.java))
        }
        //GET DEALS FOR EDITING

        val deal_title: String? = intent.getStringExtra(com.avalongh.travelmantics.ListActivity.DEAL_TITLE).toString()
        val deal_price: String? = intent.getStringExtra(com.avalongh.travelmantics.ListActivity.DEAL_PRICE).toString()
        val deal_description: String? = intent.getStringExtra(com.avalongh.travelmantics.ListActivity.DEAL_DESCRIPTION).toString()
        val deal_image: String? = intent.getStringExtra(com.avalongh.travelmantics.ListActivity.DEAL_IMAGE).toString()
        val deal_uid: String? = intent.getStringExtra(com.avalongh.travelmantics.ListActivity.DEAL_UID).toString()

        txtTitle.setText(deal_title)
        txtPrice.setText(deal_price)
        txtDescription.setText(deal_description)
        currentUID.setText(deal_uid)
        // Picasso.get().load(deal_image).into(ImageView1(this.deal_image_view1))


        supportActionBar?.title = "Add /Edit Deal" //CUSTOMISE THE TITLE OF THE ACTIVITY

        //IF DEAL IMAGE IS SELECTED, THEN PICK AN IMAGE USING INTENT
        dealImage.setOnClickListener {
            val requestCode = 0
           val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, requestCode)
        }
    }
//THIS MUST FOLLOW THE IMAGE PICKER FOR ACTION AFTER PICKING IT
     var selectedPhotoUri: Uri? = null  //so we can access it for upload into firebase database
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
        Toast.makeText(this, "Deal Image Selected", Toast.LENGTH_LONG).show()
        selectedPhotoUri = data.data
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
        dealImage.alpha = 0f
        deal_image_view1.setImageBitmap(bitmap)
      // val bitmapdrawable = BitmapDrawable(bitmap)
        //dealImage.setBackgroundDrawable(bitmapdrawable)
    }
    }

//inflate the new save menu created
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.create_deal_logout, menu)
    val insertMenu: MenuItem? = menu?.findItem(R.id.save_menu)
    val deletemenu: MenuItem? = menu?.findItem(R.id.deleteDeal)

    //CHECK IF THE USER IS ADMIN
    val myuid2 = FirebaseAuth.getInstance().uid.toString()
    //is user admin?

    var admins = startDatabase.getReference().child("/Administrators/$myuid2")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                val checks = p0?.getValue(checkresponse::class.java)
                if (checks != null){
                    insertMenu!!.setVisible(true)
                    deletemenu!!.setVisible(true)


                } else {
                    insertMenu!!.setVisible(false)
                    deletemenu!!.setVisible(false)
                }
            }

        }

        )








    return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.deleteDeal ->{
                val mycurentId = currentUID.text.toString()

                if(mycurentId != "salitsi"){
                    performDelete()
                }  else {

                    Toast.makeText(this,"Nothing to Delete",Toast.LENGTH_LONG).show()

                }
            }
            R.id.save_menu ->{

                    val mycurentId = currentUID.text.toString()

                   // performUpdate()

                    if(mycurentId == "salitsi"){
                        performInsert()
                    }else {
                        performUpdate()

                    }




            }
            R.id.signout ->{

                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        // ...
                        val intent = Intent(this, com.avalongh.travelmantics.ListActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
                        startActivity(intent)
                    }

            }






        }
    return super.onOptionsItemSelected(item)



    }


    val startDatabase = FirebaseDatabase.getInstance()
    val startStorage = FirebaseStorage.getInstance()


    private fun performInsert(){

        val txtTitle = txtTitle.text.toString()
        val txtPrice = txtPrice.text.toString()
        val txtDescription = txtDescription.text.toString()

        if(txtTitle.isEmpty() || txtDescription.isEmpty() || txtPrice.isEmpty()){
            Toast.makeText(this, "Title, Price or Description cannot be empty!", Toast.LENGTH_LONG).show()
        return
        }
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        //SAVE IMAGE TO FIREBASE STORAGE
       val refm = startStorage.getReference("/images/$filename")
        refm.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
            //LETS GET THE DOWNLOAD URL
        refm.downloadUrl.addOnSuccessListener {
          val dealImageUrl =  it.toString()

            //SAVE OTHER DETAILS TO DATABASE
            val uid = UUID.randomUUID().toString()
            val ref22 = startDatabase.getReference("/traveldeals/$uid")
          val travelDeals = traveldeals(uid,txtTitle,txtPrice,txtDescription,dealImageUrl)
            ref22.setValue(travelDeals)
                .addOnSuccessListener {
                   // Toast.makeText(this,"Deal Saved Successfully",Toast.LENGTH_LONG).show()
                    //NAVIGATE TO THE LIST ACTIVITY
                   // val intentk = Intent(this, ListActivity::class.java)
                   //intentk.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
                  //  startActivity(intentk)


                    val intent = Intent(this, com.avalongh.travelmantics.ListActivity::class.java)
                    startActivity(intent)

                }

        }

            }

    }



    private fun performUpdate(){

        val txtTitle = txtTitle.text.toString()
        val txtPrice = txtPrice.text.toString()
        val txtDescription = txtDescription.text.toString()
        val currentUid = currentUID.text.toString()

        if(txtTitle.isEmpty() || txtDescription.isEmpty() || txtPrice.isEmpty()){
            Toast.makeText(this, "Title, Price or Description cannot be empty!", Toast.LENGTH_LONG).show()
            return
        }
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        //SAVE IMAGE TO FIREBASE STORAGE
        val ref = startStorage.getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                //LETS GET THE DOWNLOAD URL
                ref.downloadUrl.addOnSuccessListener {
                    val dealImageUrl =  it.toString()

                    //SAVE OTHER DETAILS TO DATABASE
                    val uid = currentUid
                    val ref2 = startDatabase.getReference("/traveldeals/$uid")
                    val travelDeals = traveldeals(currentUid,txtTitle,txtPrice,txtDescription,dealImageUrl)
                    ref2.setValue(travelDeals)
                        .addOnSuccessListener {
                          // Toast.makeText(this,"Deal Saved Successfully",Toast.LENGTH_LONG).show()
                            //NAVIGATE TO THE LIST ACTIVITY


                                val intent = Intent(this, com.avalongh.travelmantics.ListActivity::class.java)
                                startActivity(intent)





                        }






                }


            }


    }



    private fun performDelete(){

        val currentUid = currentUID.text.toString()


                    //SAVE OTHER DETAILS TO DATABASE
                    val uid = currentUid
                    val ref2 = startDatabase.getReference("/traveldeals/$uid")
                    //val travelDeals = traveldeals(currentUid,txtTitle,txtPrice,txtDescription,dealImageUrl)
                    ref2.setValue(null)
                        .addOnSuccessListener {
                           // Toast.makeText(this,"Deal Saved Successfully",Toast.LENGTH_LONG).show()
                            //NAVIGATE TO THE LIST ACTIVITY
                           // val intentv = Intent(this, ListActivity::class.java)
                            //intentv.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
                            //startActivity(intentv)

                            val intent = Intent(this, com.avalongh.travelmantics.ListActivity::class.java)
                            startActivity(intent)


                        }
                }





    class traveldeals(val uid: String, val title: String, val price: String, val description: String, val dealimage: String) {
        constructor() : this("", "", "", "", "")
    }

    class checkresponse (val uid: String){
        constructor() : this("")
    }

}







/* //MENU ITEM AFTER INFLATING IT
override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
        R.id.menu_item -> {
            // Action goes here
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
 */