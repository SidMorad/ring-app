package mars.ring.interfaces.beacontag.helpers;

import android.content.DialogInterface;
import android.graphics.drawable.Icon;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import mars.ring.R;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.domain.model.beacontag.Category;
import mars.ring.interfaces.beacontag.BeaconTagActivity;

/**
 * Created by developer on 10/01/18.
 */
public class AlertBuilderHelper {

    public static AlertDialog beaconForm(BeaconTagActivity context, BeaconDTO dto, AlertOnButtonClickedCallback callback) {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        final ScrollView scrollView = new ScrollView(context);
        final LinearLayout lLayout = new LinearLayout(context);
        final EditText input = new EditText(context);
        final TextView category = new TextView(context);
        final RadioGroup radioGroup = new RadioGroup(context);
        final RadioButton keyType = new RadioButton(context);
        final RadioButton walletType = new RadioButton(context);
        final RadioButton bagType = new RadioButton(context);
        final RadioButton accessoriesType = new RadioButton(context);
        final RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.MATCH_PARENT);
        radioGroup.addView(keyType);
        radioGroup.addView(walletType);
        radioGroup.addView(bagType);
        radioGroup.addView(accessoriesType);
        lLayout.addView(input);
        lLayout.addView(category);
        lLayout.addView(radioGroup);
        scrollView.addView(lLayout);
        lLayout.setOrientation(LinearLayout.VERTICAL);
        params.setMarginStart(16);
        radioGroup.setLayoutParams(params);
        category.setText(context.getString(R.string.category));
        category.setPadding(10, 30, 10, 15);
        keyType.setId(R.id.radio_type_key_id);
        keyType.setButtonDrawable(R.drawable.radio_type_key);
        keyType.setChecked(true);
        keyType.setText(context.getString(R.string.keys));
        keyType.setPadding(10, 40, 40, 40);
        walletType.setId(R.id.radio_type_wallet_id);
        walletType.setButtonDrawable(R.drawable.radio_type_wallet);
        walletType.setText(context.getString(R.string.wallet));
        walletType.setPadding(10, 40, 40, 40);
        bagType.setId(R.id.radio_type_bag_id);
        bagType.setButtonDrawable(R.drawable.radio_type_bag);
        bagType.setText(context.getString(R.string.bag));
        bagType.setPadding(10, 40, 40, 40);
        accessoriesType.setId(R.id.radio_type_accessories_id);
        accessoriesType.setButtonDrawable(R.drawable.radio_type_remote);
        accessoriesType.setText(context.getString(R.string.accessories));
        accessoriesType.setPadding(10, 40, 40, 40);

        aBuilder.setView(scrollView);
        aBuilder.setTitle(context.getString(R.string.name_your_tag));
        String postitiveButtonText = context.getString(R.string.add);
        String negativeButtonText = context.getString(R.string.cancel);
        if (dto != null) {
            input.setText(dto.getTagName());
            if (dto.getCategory() != null) {
                Log.d(TAG, "Category index was : " + dto.getCategory().index());
                ((RadioButton) radioGroup.getChildAt(dto.getCategory().index())).setChecked(true);
            }
            postitiveButtonText = context.getString(R.string.edit);
            negativeButtonText = context.getString(R.string.delete);
        }
        aBuilder.setPositiveButton(postitiveButtonText, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            String tagName = input.getText().toString();
            Log.d(TAG, "Entered tagName is " + tagName);
            int catIndex = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
            Log.d(TAG, "Entered categoryIndex is " + catIndex);
            callback.onPositiveClick(tagName, catIndex, dto);
          }
        });
        aBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int i) {
              callback.onNegativeClick(dto);
              dialog.cancel();
          }
        });
        AlertDialog dialog = aBuilder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    public static AlertDialog toggleBeaconIsMissingForm(BeaconTagActivity context, BeaconDTO dto, AlertOnButtonClickedCallback callback) {
        AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
        final LinearLayout lLayout = new LinearLayout(context);
        final TextView tagName = new TextView(context);
        final ImageButton category = new ImageButton(context);
        lLayout.addView(category);
        lLayout.addView(tagName);
        lLayout.setOrientation(LinearLayout.HORIZONTAL);
        lLayout.setGravity(Gravity.CENTER_VERTICAL);

        aBuilder.setView(lLayout);
        aBuilder.setTitle(context.getString(R.string.mark_as_lost));
        String positiveButtonText = context.getString(R.string.mark);
        String negativeButtonText = context.getString(R.string.cancel);
        if (dto != null) {
            tagName.setText(dto.getTagName());
            if (dto.getCategory() != null) {
                Log.d(TAG, "Category index was : " + dto.getCategory().index());
                category.setImageResource(Category.res(dto.getCategory()));
            }
            if (dto.isMissing()) {
                aBuilder.setTitle(context.getString(R.string.mark_as_found));
            }
        }
        aBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.onPositiveClick(null, 0, dto);
            }
        });
        aBuilder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                callback.onNegativeClick(dto);
                dialog.cancel();
            }
        });
        AlertDialog dialog = aBuilder.create();
        return dialog;
    }

    private static final String TAG = AlertBuilderHelper.class.getSimpleName() + "1";
}
