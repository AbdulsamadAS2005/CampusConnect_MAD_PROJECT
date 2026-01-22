package com.example.mad_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_project.R;
import com.example.mad_project.adapters.PostAdapter;
import com.example.mad_project.models.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private ProgressBar progressBar;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        // Initialize views
        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(requireContext(), postList);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewPosts.setAdapter(postAdapter);

        // Load posts
        loadPosts();

        return view;
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);

        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null) {
                        if (post.getPostId() == null) {
                            post.setPostId(snapshot.getKey());
                        }
                        postList.add(post);
                    }
                }
                // Sort by timestamp (newest first)
                Collections.sort(postList, (p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}