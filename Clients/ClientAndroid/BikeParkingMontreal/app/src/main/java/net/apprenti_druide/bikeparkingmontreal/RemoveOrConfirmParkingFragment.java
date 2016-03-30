package net.apprenti_druide.bikeparkingmontreal;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class RemoveOrConfirmParkingFragment extends DialogFragment {

    public String bikeParkingId;

    public interface RemoveOrConfirmParkingListener
    {
        void RemoveBikeParkingEvent(String id);
        void ConfirmBikeParkingEvent(String id);
    }

    private RemoveOrConfirmParkingListener listener;

    public static RemoveOrConfirmParkingFragment newInstance() {
        RemoveOrConfirmParkingFragment fragment = new RemoveOrConfirmParkingFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_remove_or_confirm_parking, container);

        Button confirmButton = (Button) v.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                listener.ConfirmBikeParkingEvent(bikeParkingId);
            }
        });

        Button removeButton = (Button) v.findViewById(R.id.delete_button);
        removeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                listener.RemoveBikeParkingEvent(bikeParkingId);
            }
        });

        getDialog().setTitle("Confirm or Delete this location");




        return v;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (RemoveOrConfirmParkingListener) activity;
    }


}
