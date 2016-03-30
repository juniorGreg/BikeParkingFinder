package net.apprenti_druide.bikeparkingmontreal;

import android.app.Activity;

import android.app.DialogFragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;




public class AddBikeParkingFragment extends DialogFragment{

    public interface onAddBikeParkingListener {
        void addBikeParkingEvent(int capacity);
    }

    onAddBikeParkingListener addBikeParkingEventListener;
    EditText editText;

    public static AddBikeParkingFragment newInstance() {
        AddBikeParkingFragment fragment = new AddBikeParkingFragment();

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            addBikeParkingEventListener = (onAddBikeParkingListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_bike_parking, container);

        Button button = (Button) v.findViewById(R.id.add_bike_parking_button);
        editText = (EditText) v.findViewById(R.id.capacityText);
        getDialog().setTitle("Add a Bike Parking");


        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                int capacity = Integer.parseInt(editText.getText().toString());
                addBikeParkingEventListener.addBikeParkingEvent(capacity);

            }
        });




        return v;
    }
}
