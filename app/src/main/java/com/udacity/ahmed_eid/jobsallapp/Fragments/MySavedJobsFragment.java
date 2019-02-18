package com.udacity.ahmed_eid.jobsallapp.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.ahmed_eid.jobsallapp.Adapters.JobAdapter;
import com.udacity.ahmed_eid.jobsallapp.Model.AppliedJob;
import com.udacity.ahmed_eid.jobsallapp.Model.Job;
import com.udacity.ahmed_eid.jobsallapp.Model.SavedJob;
import com.udacity.ahmed_eid.jobsallapp.R;

import java.util.ArrayList;


public class MySavedJobsFragment extends Fragment {
    
    private RecyclerView savedJobsRecycler ;
    private FirebaseAuth mAuth ;
    private DatabaseReference mDatabase ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView;
        myView = inflater.inflate(R.layout.fragment_my_saved_jobs, container, false);
        this.savedJobsRecycler = myView.findViewById(R.id.savedJobs_recycler);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference() ;
        readSavedJobs();
        return myView;
        
    }

    private void readSavedJobs() {
        String userId = mAuth.getCurrentUser().getUid();
        final ArrayList<SavedJob> savedJobs  = new ArrayList<>();
        Query query = mDatabase.child("SavedJobs").child(userId).orderByChild("userId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        SavedJob savedJob = snapshot.getValue(SavedJob.class);
                        savedJobs.add(savedJob);
                    }
                    readJobsThisUser(savedJobs);
                }else {
                    Toast.makeText(getActivity(), "Not Found Saved Jobs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                String massage = error.getMessage();
                Toast.makeText(getActivity(), "Error: " + massage, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void readJobsThisUser(final ArrayList<SavedJob> savedJobs) {
        final ArrayList<Job> jobs =new ArrayList<>();
        for (SavedJob savedJob : savedJobs){
           String jobId =  savedJob.getJobId();
            Query query = mDatabase.child("Jobs").orderByChild("jobId").equalTo(jobId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Job job = snapshot.getValue(Job.class);
                            jobs.add(job);
                        }
                        int jobSize = jobs.size();
                        int appliedJobSize = savedJobs.size();
                        if (appliedJobSize == jobSize){
                            setDataToRecyclerAdapter(jobs);
                        }
                    }else {
                        Toast.makeText(getActivity(), "Not Found a Jobs", Toast.LENGTH_SHORT).show();
                        Log.e("applayed Frag","Not Found a Jobs");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    String massage = error.getMessage();
                    Toast.makeText(getActivity(), "Error: " + massage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void setDataToRecyclerAdapter(ArrayList<Job> jobs) {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        savedJobsRecycler.setLayoutManager(manager);
        JobAdapter jobAdapter = new JobAdapter(getActivity(), jobs);
        savedJobsRecycler.setAdapter(jobAdapter);
    }
}
