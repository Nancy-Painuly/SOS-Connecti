package com.example.myfamily

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myfamily.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var inviteAdapter : InviteAdapter
    lateinit var mContext: Context

    private val listContacts: ArrayList<ContactModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater,container,false)

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listMembers = listOf(
                MemberModel(
                    "Naina",
                    "9th buildind, 2nd floor, maldiv road, manali 9th buildind, 2nd floor",
                    "90%",
                    "220"
                ),
                MemberModel(
                    "Tae",
                    "10th buildind, 3rd floor, maldiv road, manali 10th buildind, 3rd floor",
                    "80%",
                    "210"
                ),
                MemberModel(
                    "SHMILY",
                    "11th buildind, 4th floor, maldiv road, manali 11th buildind, 4th floor",
                    "70%",
                    "200"
                ),
                MemberModel(
                    "Painuly",
                    "12th buildind, 5th floor, maldiv road, manali 12th buildind, 5th floor",
                    "60%",
                    "190"
                ),
        )


        val adapter = MemberAdapter(listMembers)


        binding.recyclerMember.layoutManager = LinearLayoutManager(mContext)
        binding.recyclerMember.adapter = adapter


        Log.d("fetchContact89", "fetchContacts: start karna wale hain")

        Log.d("fetchContact89", "fetchContacts: start hogya hai ${listContacts.size}")
        inviteAdapter = InviteAdapter(listContacts)
        fetchDatabaseContacts()
        Log.d("fetchContact89", "fetchContacts: end hogya hai")

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("fetchContact89", "fetchContacts: coroutine start")

            insertDatabaseContacts(fetchContacts()) //takes all contacts and insert it in database

            Log.d("fetchContact89", "fetchContacts: coroutine end ${listContacts.size}")
        }



        binding.recyclerInvite.layoutManager = LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerInvite.adapter = inviteAdapter

        val threeDots = requireView().findViewById<ImageView>(R.id.icon_three_dots)
        threeDots.setOnClickListener{

            SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN,false)

            FirebaseAuth.getInstance().signOut()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDatabaseContacts() {
        val database = MyFamilyDatabase.getDatabase(requireContext())

      database.contactDao().getAllContacts().observe(viewLifecycleOwner){

          Log.d("fetchContact89", "fetchDatabaseContacts: ")

          listContacts.clear()
          listContacts.addAll(it)

          inviteAdapter.notifyDataSetChanged()

      }
    }

    private suspend fun insertDatabaseContacts(listContacts: ArrayList<ContactModel>) {


        val database = MyFamilyDatabase.getDatabase(mContext)

        database.contactDao().insertAll(listContacts)
    }

    private fun fetchContacts() : ArrayList<ContactModel>{

        Log.d("fetchContact89", "fetchContacts: start")
        val cr= requireActivity().contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null)
        val listContacts:ArrayList<ContactModel> = ArrayList()
        if(cursor!=null && cursor.count>0){
            while((cursor != null) && cursor.moveToNext()){
                val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                if(hasPhoneNumber>0){

                    val pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",
                        arrayOf(id),
                        ""
                    )

                    if(pCur !=null && pCur.count>0){

                        while(( pCur!=null ) && pCur.moveToNext()){

                            val phoneNum = pCur.getString(pCur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))

                            listContacts.add(ContactModel(name,phoneNum))
                        }
                        pCur.close()
                    }
                }
            }
            if(cursor!=null){
                cursor.close()
            }
        }
        Log.d("fetchContact89", "fetchContacts: end")
        return listContacts
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}