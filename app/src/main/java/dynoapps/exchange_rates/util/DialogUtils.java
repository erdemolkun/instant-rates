package dynoapps.exchange_rates.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import dynoapps.exchange_rates.R;


/**
 * Created by eolkun on 10.12.2014.
 */
public class DialogUtils {


    public static DialogBuilder getDialogBuilder(Context context) {
        return new DialogBuilder(context);
    }


    /**
     * Custom dialog builder to create non standard views.
     */
    public static class DialogBuilder {
        private String title;
        private String message;
        private String positiveText, negativeText, neutralText;
        private Dialog.OnClickListener dialogClickListener;
        private Dialog.OnCancelListener dialogCancelListener;
        private DialogInterface.OnDismissListener dismissListener;
        private Integer iconResource;
        private Drawable drawable;
        private boolean autoDismissOnClick = true;


        private
        @LayoutRes
        Integer customViewRes;

        private View customView;

        private Context context;

        private DialogBuilder(Context context) {
            this.context = context;
        }

        public DialogBuilder title(String title) {
            this.title = title;
            return this;
        }

        public DialogBuilder title(@StringRes Integer titleRes) {
            return title(context.getString(titleRes));
        }

        public DialogBuilder autoDissmissOnClick(boolean autoDismissOnClick) {
            this.autoDismissOnClick = autoDismissOnClick;
            return this;
        }

        public DialogBuilder customView(@LayoutRes Integer customViewRes) {
            this.customViewRes = customViewRes;
            return this;
        }

        public DialogBuilder customView(View customView) {
            this.customView = customView;
            return this;
        }

        public DialogBuilder message(String message) {
            this.message = message;
            return this;
        }

        public DialogBuilder message(@StringRes Integer messageRes) {
            return message(context.getString(messageRes));
        }

        public DialogBuilder positive(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public DialogBuilder positive(@StringRes Integer positiveRes) {
            return positive(context.getString(positiveRes));
        }

        public DialogBuilder neutral(String neutralText) {
            this.neutralText = neutralText;
            return this;
        }

        public DialogBuilder neutral(@StringRes Integer neutralRes) {
            return neutral(context.getString(neutralRes));
        }

        public DialogBuilder negative(String negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        public DialogBuilder negative(@StringRes Integer negativeRes) {
            return negative(context.getString(negativeRes));
        }

        public DialogBuilder negative(String negativeText, Dialog.OnClickListener dialogClickListener) {
            this.negativeText = negativeText;
            this.dialogClickListener = dialogClickListener;
            return this;
        }

        public DialogBuilder positive(String positiveText, Dialog.OnClickListener dialogClickListener) {
            this.positiveText = positiveText;
            this.dialogClickListener = dialogClickListener;
            return this;
        }

        public DialogBuilder listener(Dialog.OnClickListener dialogClickListener) {
            this.dialogClickListener = dialogClickListener;
            return this;
        }

        public DialogBuilder dismissListener(DialogInterface.OnDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        public DialogBuilder setCancelListener(DialogInterface.OnCancelListener cancelListener) {
            this.dialogCancelListener = cancelListener;
            return this;
        }

        public DialogBuilder icon(@DrawableRes Integer iconResource) {
            this.drawable = null;
            this.iconResource = iconResource;
            return this;
        }

        public DialogBuilder iconDrawable(Drawable icon) {
            this.iconResource = null;
            this.drawable = icon;
            return this;
        }

        public Dialog build() {
            return buildDialog();
        }

        public Dialog show() {
            Dialog d = buildDialog(); // Using android default themed dialog for now
            d.getWindow().setWindowAnimations(R.style.DialogAnimationFade);
            try {
                d.show();
                if (dismissListener != null) {
                    d.setOnDismissListener(dismissListener);
                }
            } catch (Exception ex) {
                L.ex(ex);
            }
            return d;
        }

        private Dialog buildDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialog);
            if (title != null) {
                builder.setTitle(title);
            }
            if (message != null) {
                builder.setMessage(message);
            }
            if (positiveText != null) {
                builder.setPositiveButton(positiveText, autoDismissOnClick ? dialogClickListener : null);
            }
            if (negativeText != null) {
                builder.setNegativeButton(negativeText, autoDismissOnClick ? dialogClickListener : null);
            }
            if (neutralText != null) {
                builder.setNeutralButton(neutralText, autoDismissOnClick ? dialogClickListener : null);
            }
            if (dialogCancelListener != null) {
                builder.setOnCancelListener(dialogCancelListener);
            }
            if (iconResource != null) {
                builder.setIcon(iconResource);
            }
            if (drawable != null) {
                builder.setIcon(drawable);
            }
            if (customViewRes != null) {
                builder.setView(customViewRes);
            } else if (customView != null) {
                builder.setView(customView);
            }
            final AlertDialog alertDialog = builder.create();
            if (!autoDismissOnClick && dialogClickListener != null)
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialogInterface) {
                        Button bPos = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        bPos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogClickListener.onClick(dialogInterface, AlertDialog.BUTTON_POSITIVE);
                            }
                        });

                        Button bNeg = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        bNeg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialogClickListener.onClick(dialogInterface, AlertDialog.BUTTON_NEGATIVE);
                            }
                        });
                    }
                });
            return alertDialog;
        }
    }
}
