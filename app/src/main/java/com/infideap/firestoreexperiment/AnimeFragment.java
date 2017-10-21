package com.infideap.firestoreexperiment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AnimeFragment extends Fragment {

    private static final String TAG = AnimeFragment.class.getSimpleName();
    private OnListFragmentInteractionListener mListener;
    private FirebaseFirestore db;
    private List<Anime> set;
    private AnimeRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AnimeFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AnimeFragment newInstance() {

        return new AnimeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anime_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            //Not working, change to list
            set = new ArrayList<>();
            adapter = new AnimeRecyclerViewAdapter(set, mListener);
            recyclerView.setAdapter(adapter);

            retrieveDatas();
        }

        return view;
    }

    private void retrieveDatas() {
        final CollectionReference docRef = db.collection("anime");
        docRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null) {
                    Log.d(TAG, "Doc : " + snapshot.getDocuments().size());
                    // Maybe this code to retrieve new item added
                    Log.d(TAG, "Doc Change: " + snapshot.getDocumentChanges().size());

                    if (set.size() == 0) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Anime anime = doc.toObject(Anime.class);
                            anime.key = doc.getId();
                            add(set, anime);
                        }
                        adapter.notifyItemRangeInserted(0, set.size());
                    } else {
                        for (DocumentChange change : snapshot.getDocumentChanges()) {
                            DocumentSnapshot doc = change.getDocument();
                            Anime anime = doc.toObject(Anime.class);
                            anime.key = doc.getId();
                            switch (change.getType()) {
                                case ADDED:
                                    adapter.notifyItemInserted(add(set, anime));
                                    break;
                                case MODIFIED:
                                    Anime anime1 = find(set, anime);
                                    if (anime1 != null) {
                                        anime1.title = anime.title;
                                        anime1.description = anime.description;
                                        anime1.score = anime.score;
                                        anime1.noOfEpisode = anime.noOfEpisode;
                                        anime1.isFinish = anime.isFinish;
                                        anime1.genre = anime.genre;
                                        adapter.notifyItemChanged(set.indexOf(anime1));
                                    }
                                    break;
                                case REMOVED:
                                    int index = remove(set, anime);
                                    if (index > -1)
                                        adapter.notifyItemChanged(index);
                                    break;

                            }

                        }
                        adapter.notifyItemRangeChanged(0, set.size());

                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private int remove(List<Anime> set, Anime anime) {
        int index = 0;
        for (Anime anime1 : set) {
            if (anime1.key.equals(anime.key)) {
                set.remove(anime1);
                return index;
            }
            index++;
        }
        return -1;
    }

    private int add(List<Anime> set, Anime anime) {
        int index = 0;
        for (Anime anime1 : set) {
            if (anime.score > anime1.score) {
                set.add(index, anime);
                return index;
            }
            index++;
        }
        set.add(anime);
        return index;
    }

    private Anime find(List<Anime> set, Anime anime) {
        for (Anime anime1 : set) {
            if (anime1.key.equals(anime.key)) {
                return anime1;
            }
        }
        return null;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnListFragmentInteractionListener {
    }
}
