package com.xixia.appetizing.UI;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.vision.Frame;
import com.xixia.appetizing.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InstructionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InstructionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructionFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    @BindView(R.id.closeInstructions) ImageButton mCloseInstructions;
    private FrameLayout mInstructionFrame;

    public InstructionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InstructionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InstructionFragment newInstance() {
        InstructionFragment fragment = new InstructionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstructionFrame = this.getActivity().findViewById(R.id.instructionFragmentFrame);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        mCloseInstructions.setOnClickListener(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if(view == mCloseInstructions){
            mInstructionFrame.setVisibility(View.GONE);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
