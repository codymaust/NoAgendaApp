package us.k117.noagendaapp.ui;

import us.k117.noagendaapp.R;
import us.k117.noagendaapp.R.layout;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.app.Fragment;

public class LiveStreamFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("Test", "hello");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_livestream, container, false);
		//TextView textView = (TextView) view.findViewById(R.id.liveStreamText);
		//textView.setText("Live Stream");
		return view;
	}
}
