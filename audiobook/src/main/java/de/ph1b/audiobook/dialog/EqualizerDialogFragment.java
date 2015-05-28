package de.ph1b.audiobook.dialog;

import android.app.Dialog;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import de.ph1b.audiobook.R;
import de.ph1b.audiobook.mediaplayer.MediaPlayerController;


public class EqualizerDialogFragment extends DialogFragment {
    public static final String TAG = EqualizerDialogFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LinearLayout customView = new LinearLayout(getActivity());
        customView.setOrientation(LinearLayout.VERTICAL);

        final Equalizer equalizer = new Equalizer(0, MediaPlayerController.AUDIO_SESSION_ID);
        equalizer.setEnabled(true);

        final short minEQLevel = equalizer.getBandLevelRange()[0];
        final short maxEQLevel = equalizer.getBandLevelRange()[1];

        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
            final short band = i;

            LinearLayout item = new LinearLayout(getActivity());
            TextView freqTextView = new TextView(getActivity());
            SeekBar bar = new SeekBar(getActivity());
            bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            item.addView(freqTextView);
            item.addView(bar);

            freqTextView.setText((equalizer.getCenterFreq(band) / 1000) + " Hz");

            bar.setMax(maxEQLevel - minEQLevel);
            short level = equalizer.getBandLevel(band);
            bar.setProgress(level - minEQLevel);
            bar.setEnabled(equalizer.getEnabled());

            bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    short level = (short) (progress + minEQLevel);
                    if (fromUser) {
                        equalizer.setBandLevel(band, level);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            customView.addView(item);
        }

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.equalizer)
                .customView(customView, true)
                .build();
    }


}
