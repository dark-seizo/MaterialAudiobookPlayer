package de.ph1b.audiobook.dialog;

import android.app.Dialog;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import de.ph1b.audiobook.R;
import de.ph1b.audiobook.model.Book;
import de.ph1b.audiobook.model.DataBaseHelper;
import de.ph1b.audiobook.uitools.ThemeUtil;


public class EqualizerDialogFragment extends DialogFragment {
    public static final String TAG = EqualizerDialogFragment.class.getSimpleName();
    public static final String BOOK_ID = "BOOK_ID";


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LinearLayout customView = new LinearLayout(getActivity());
        customView.setOrientation(LinearLayout.VERTICAL);

        final DataBaseHelper db = DataBaseHelper.getInstance(getActivity());
        final long bookId = getArguments().getLong(BOOK_ID);
        final Book book = db.getBook(bookId);
        if (book == null) {
            throw new IllegalArgumentException(TAG + " started without a valid book for bookId=" + bookId);
        }

        // equalizer container
        TableLayout equalizerContainer = new TableLayout(getActivity());
        equalizerContainer.setOrientation(LinearLayout.VERTICAL);
        TableLayout.LayoutParams containerLayoutParams = new TableLayout.LayoutParams();
        containerLayoutParams.width = TableLayout.LayoutParams.MATCH_PARENT;
        containerLayoutParams.height = TableLayout.LayoutParams.MATCH_PARENT;
        equalizerContainer.setLayoutParams(containerLayoutParams);
        customView.addView(equalizerContainer);

        final Equalizer equalizer = new Equalizer(0, 52);
        final short minEQLevel = equalizer.getBandLevelRange()[0];
        final short maxEQLevel = equalizer.getBandLevelRange()[1];
        for (short i = 0; i < equalizer.getNumberOfBands(); i++) {
            final short band = i;

            // init row
            TableRow tableRow = new TableRow(getActivity());
            TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams();
            tableRow.setLayoutParams(tableRowLayoutParams);
            equalizerContainer.addView(tableRow);

            // init text
            TextView frequencyView = new TextView(getActivity());
            frequencyView.setTextColor(getResources().getColor(ThemeUtil.getResourceId(getActivity(),
                    R.attr.text_secondary)));
            frequencyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension
                    (R.dimen.list_text_secondary_size));
            TableRow.LayoutParams frequencyLayoutParams = new TableRow.LayoutParams();
            frequencyLayoutParams.gravity = Gravity.CENTER_VERTICAL;
            frequencyView.setLayoutParams(frequencyLayoutParams);
            tableRow.addView(frequencyView);

            // init seekBar
            SeekBar seekBar = new SeekBar(getActivity());
            TableRow.LayoutParams seekBarLayoutParams = new TableRow.LayoutParams();
            seekBarLayoutParams.weight = 1;
            seekBar.setLayoutParams(seekBarLayoutParams);
            tableRow.addView(seekBar);

            // set text
            frequencyView.setText((equalizer.getCenterFreq(band) / 1000) + " Hz");

            // set seekBar
            seekBar.setMax(maxEQLevel - minEQLevel);
            short level = book.getBandLevel(band);
            if (level == -1) { // if band has not been set yet, set default band
                level = equalizer.getBandLevel(band);
            }
            seekBar.setProgress(level - minEQLevel);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        final short level = (short) (progress + minEQLevel);
                        synchronized (db) {
                            Book book = db.getBook(bookId);
                            if (book != null) {
                                book.setBandLevel(band, level);
                                db.updateBook(book);
                            }
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.equalizer)
                .customView(customView, true)
                .build();
    }
}
