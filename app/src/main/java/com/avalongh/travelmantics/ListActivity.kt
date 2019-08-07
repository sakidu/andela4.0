package com.avalongh.travelmantics

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.list_deals.view.*
import android.view.MenuItem as MenuItem1
import com.google.firebase.database.ValueEventListener as ValueEventListener1

class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        val myuid = FirebaseAuth.getInstance().uid



        if(myuid ==null) {
            createSignInIntent()
        }



        //FIRST LETS CHECK IF USER IS LOGGED IN
      /*
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null){
            //send user to register page
            val intent = Intent(this, loginActivity::class.java)
        // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
        startActivity(intent)
        }

        */


        recyclereview_listdeals.layoutManager = LinearLayoutManager(this)
        val adapter = GroupAdapter<ViewHolder>()  //create adapter and the viewholders




     //GET THE DEALS FROM THE DATABASE
        val ref3 = FirebaseDatabase.getInstance().getReference("/traveldeals")
        ref3.addListenerForSingleValueEvent(object: ValueEventListener1 {
            override fun onDataChange(p0: DataSnapshot) {
                p0!!.children.forEach{
                   // Log.d("Snapshot",it.toString())
                    val deals = it.getValue(traveldeals2::class.java)
                   if(deals != null) {
                       adapter.add(ListDeal(deals))
                   }
                }
                adapter.setOnItemClickListener { item, view ->
                    val ListDeal = item as ListDeal
                    val intent = Intent(view.context, InsertActivity::class.java)
                    intent.putExtra(DEAL_TITLE, ListDeal.deals.title)
                    intent.putExtra(DEAL_PRICE, ListDeal.deals.price)
                    intent.putExtra(DEAL_DESCRIPTION, ListDeal.deals.description)
                    intent.putExtra(DEAL_IMAGE, ListDeal.deals.dealimage)
                    intent.putExtra(DEAL_UID, ListDeal.deals.uid)
                    //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
                    startActivity(intent)
                }
                recyclereview_listdeals.adapter = adapter

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }





    private fun createSignInIntent() {

        // [START auth_fui_create_intent]

        // Choose authentication providers

        val providers = arrayListOf(

            AuthUI.IdpConfig.EmailBuilder().build(),

            //AuthUI.IdpConfig.PhoneBuilder().build(),

            AuthUI.IdpConfig.GoogleBuilder().build())



        // Create and launch sign-in intent

        startActivityForResult(

            AuthUI.getInstance()

                .createSignInIntentBuilder()

                .setAvailableProviders(providers)

                .build(),

            RC_SIGN_IN)

        // [END auth_fui_create_intent]

    }



    // [START auth_fui_result]

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)



        if (requestCode == RC_SIGN_IN) {

            val response = IdpResponse.fromResultIntent(data)



            if (resultCode == Activity.RESULT_OK) {

                // Successfully signed in

                val user = FirebaseAuth.getInstance().currentUser

                //RELOAD THE ACTIVITY
                startActivity(Intent(this, ListActivity::class.java))

                // ...

            } else {

                // Sign in failed. If response is null the user canceled the

                // sign-in flow using the back button. Otherwise check

                // response.getError().getErrorCode() and handle the error.

                // ...

            }

        }

    }

    // [END auth_fui_result]



    private fun signOut() {

        // [START auth_fui_signout]

        AuthUI.getInstance()

            .signOut(this)

            .addOnCompleteListener {

                // ...

            }

        // [END auth_fui_signout]

    }



    private fun delete() {

        // [START auth_fui_delete]

        AuthUI.getInstance()

            .delete(this)

            .addOnCompleteListener {

                // ...

            }

        // [END auth_fui_delete]

    }





    private fun privacyAndTerms() {

        val providers = emptyList<AuthUI.IdpConfig>()

        // [START auth_fui_pp_tos]

        startActivityForResult(

            AuthUI.getInstance()

                .createSignInIntentBuilder()

                .setAvailableProviders(providers)

                .setTosAndPrivacyPolicyUrls(

                    "https://example.com/terms.html",

                    "https://example.com/privacy.html")

                .build(),

            RC_SIGN_IN)

        // [END auth_fui_pp_tos]

    }


























    //CREATE COMPANION OBJECTS FOR THE SENDING THE EXTRAS TO THE INSERT ACTIVITY - THESE ARE THE KEYS SPECIFIED IN THE EXTRAS INTENT
    companion object{
        val DEAL_TITLE = "DEAL TITLE"
        val DEAL_PRICE = "DEAL PRICE"
        val DEAL_DESCRIPTION = "DEAL DESCRIPTION"
        val DEAL_IMAGE = "DEAL IMAGE"
        val DEAL_UID = "DEAL UID"
        val RC_SIGN_IN = 123
    }










    override fun onOptionsItemSelected(item: MenuItem1): Boolean {
       when (item?.itemId){
           R.id.signout ->{

               AuthUI.getInstance()
                   .signOut(this)
                   .addOnCompleteListener {
                       // ...
                       val intent = Intent(this, ListActivity::class.java)
                       intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
                       startActivity(intent)                   }


           }
           R.id.save_menu ->{
               val intent = Intent(this,InsertActivity::class.java)
               intent.putExtra(DEAL_TITLE, "")
               intent.putExtra(DEAL_PRICE, "")
               intent.putExtra(DEAL_DESCRIPTION, "")
               intent.putExtra(DEAL_IMAGE, "")
               intent.putExtra(DEAL_UID, "salitsi").toString()
               //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears the old activity from stack. Back bottom does not take you back to the previous activity
               startActivity(intent)
           }
       }
        return super.onOptionsItemSelected(item)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_deal_logout,menu)
        val insertMenu: MenuItem1? = menu?.findItem(R.id.save_menu)
        val deletemenu: MenuItem1? = menu?.findItem(R.id.deleteDeal)

        //CHECK IF THE USER IS ADMIN
        val myuid2 = FirebaseAuth.getInstance().uid.toString()
        //is user admin?

        var admins = FirebaseDatabase.getInstance().getReference().child("/Administrators/$myuid2")
            .addListenerForSingleValueEvent(object : ValueEventListener1 {
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

            })



       // val admintoString = admins.toString()

        deletemenu!!.setVisible(false) //BY DEFAULT LETS DISABLE THIS MENU HERE
    return super.onCreateOptionsMenu(menu)

    }



}

class ListDeal(val deals: traveldeals2): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.grab_deal_title.text = deals.title
        val priceCurrency = "USD " + deals.price
        viewHolder.itemView.grab_deal_price.text = priceCurrency
        viewHolder.itemView.grab_Deal_description.text = deals.description
        val myuid= deals.uid
        Picasso.get().load(deals.dealimage).into(viewHolder.itemView.grab_Deal_image)
    }
    override fun getLayout(): Int {
     return R.layout.list_deals
    }

}

class traveldeals2(val uid: String, val title: String, val price: String, val description: String, val dealimage: String){
    constructor() : this("","","","","")
}

class checkresponse (val uid: String){
    constructor() : this("")
}