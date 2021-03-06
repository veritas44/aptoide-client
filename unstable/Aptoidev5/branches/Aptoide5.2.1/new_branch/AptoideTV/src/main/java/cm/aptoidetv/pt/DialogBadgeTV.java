package cm.aptoidetv.pt;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Iterator;

import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;

/**
 * Created by tdeus on 1/16/14.
 */
public class DialogBadgeTV extends DialogFragment {


    public DialogBadgeTV() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_anti_malware, null);
        String appName = getArguments().getString("appName");
        String status = getArguments().getString("status");
        GetApkInfoJson.Malware.Reason reason = ((AppViewActivityTV) getActivity()).getReason();
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(status.equals("scanned")?getString(R.string.app_trusted, appName):getString(R.string.app_warning, appName))
                .setIcon(status.equals("scanned")?getResources().getDrawable(R.drawable.ic_trusted):getResources().getDrawable(R.drawable.ic_warning))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        if (reason != null) {
            if (reason.getScanned() != null && reason.getScanned().getStatus()!=null && (reason.getScanned().getStatus().equals("passed") || reason.getScanned().getStatus().equals("warn"))) {

                if (reason.getScanned().getAv_info() != null) {
                    StringBuffer av = new StringBuffer();
                    Iterator<GetApkInfoJson.Av_info> iterator = reason.getScanned().getAv_info().iterator();
                    while (iterator.hasNext()) {
                        GetApkInfoJson.Av_info av_info = iterator.next();
                        av.append(av_info.getName());
                        if (iterator.hasNext()) {
                            av.append(", ");
                        }
                    }
                    
                    v.findViewById(R.id.reason_scanned_description).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.reason_scanned).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_scanned_description)).setText(getString(R.string.scanned_with_av));
                    ((TextView) v.findViewById(R.id.reason_scanned)).setText(av.toString());
                }
            }

            if (reason.getThirdparty_validated() != null) {
                v.findViewById(R.id.reason_thirdparty_validated_description).setVisibility(View.VISIBLE);
                v.findViewById(R.id.reason_thirdparty_validated).setVisibility(View.VISIBLE);
                ((TextView) v.findViewById(R.id.reason_thirdparty_validated_description)).setText(getString(R.string.compared_with_another_marketplace));
                ((TextView) v.findViewById(R.id.reason_thirdparty_validated)).setText(reason.getThirdparty_validated().getStore());
            }

            if (reason.getSignature_validated() != null && reason.getSignature_validated().getStatus()!=null ) {

                if(reason.getSignature_validated().getStatus().equals("passed")){
                    v.findViewById(R.id.reason_signature_validation_description).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_signature_validation_description)).setText(getString(R.string.application_signature_analysis));
                    v.findViewById(R.id.reason_signature_validated).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_signature_validated)).setText(getString(R.string.application_signature_matched));
                }else if(reason.getSignature_validated().getStatus().equals("failed")){
                    v.findViewById(R.id.reason_signature_validation_description).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_signature_validation_description)).setText(getString(R.string.application_signature_analysis));
                    v.findViewById(R.id.reason_signature_not_validated).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_signature_not_validated)).setText(getString(R.string.application_signature_not_matched));
                }else if(reason.getSignature_validated().getStatus().equals("blacklisted")){
                    v.findViewById(R.id.reason_signature_validation_description).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_signature_validation_description)).setText(getString(R.string.application_signature_analysis));
                    v.findViewById(R.id.reason_signature_not_validated).setVisibility(View.VISIBLE);
                    ((TextView) v.findViewById(R.id.reason_signature_not_validated)).setText(getString(R.string.application_signature_blacklisted));
                }
            }

            if (reason.getManual_qa() != null && reason.getManual_qa().getStatus()!=null && reason.getManual_qa().getStatus().equals("passed")) {
                v.findViewById(R.id.reason_manual_qa_description).setVisibility(View.VISIBLE);
                v.findViewById(R.id.reason_manual_qa).setVisibility(View.VISIBLE);
                ((TextView) v.findViewById(R.id.reason_manual_qa_description)).setText(getString(R.string.scanned_manually_by_aptoide_team));
                ((TextView) v.findViewById(R.id.reason_manual_qa)).setText(getString(R.string.scanned_verified_by_tester));
            }


        }

        return builder;
    }

}
