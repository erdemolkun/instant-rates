package dynoapps.exchange_rates.util;

import android.app.Activity;
import android.support.annotation.IntDef;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dynoapps.exchange_rates.R;

/**
 * Created by eolkun on 10.11.2015.
 * <p/>
 * Helper class to manage animation resources.
 */
public class AnimationHelper {

    public static final int NONE = 0;
    public static final int FADE_IN = 1;
    public static final int FROM_BOTTOM_ON_OVER = 2;
    public static final int SLIDE_LEFT_IN = 3;
    public static final int LIKE_VINE = 4;
    public static final int LEFT_IN = 5;
    public static final int GROW_BOTTOM = 6;
    public static final int FROM_BOTTOM_PUSH_OUT = 7;
    public static final int JUMP_SCALE = 8;

    public static void doAnimation(Activity activity, @AnimationType int animationType, boolean enter) {
        if (animationType == NONE) {
            activity.overridePendingTransition(0, 0);
        } else if (animationType == FADE_IN) {
            doAnimFade(activity, enter);
        } else if (animationType == FROM_BOTTOM_ON_OVER) {
            doAnimFromBottomOnOver(activity, enter);
        } else if (animationType == SLIDE_LEFT_IN) {
            doAnimSlideLeftIn(activity, enter);
        } else if (animationType == LIKE_VINE) {
            doAnimLikeVine(activity, enter);
        } else if (animationType == LEFT_IN) {
            doAnimLeftIn(activity, enter);
        } else if (animationType == GROW_BOTTOM) {
            doGrowFromBottom(activity, enter);
        } else if (animationType == FROM_BOTTOM_PUSH_OUT) {
            doAnimFromBottomPushOut(activity, enter);
        } else if (animationType == JUMP_SCALE) {
            doJumpScale(activity, enter);
        }
    }

    private static void doAnimLeftIn(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.activity_slide_right_in, R.anim.activity_slide_left_out);
        } else {
            activity.overridePendingTransition(R.anim.activity_slide_left_in, R.anim.activity_slide_right_out);
        }
    }

    private static void doAnimLikeVine(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
        } else {
            activity.overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);
        }
    }

    private static void doAnimSlideLeftIn(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.activity_slide_scale_fade_left_in, R.anim.activity_slide_scale_fade_left_out);
        } else {
            activity.overridePendingTransition(R.anim.activity_slide_scale_fade_right_in, R.anim.activity_slide_scale_fade_right_out);
        }
    }

    private static void doAnimFade(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.fade_in, R.anim.no_animation_long);
        } else {
            activity.overridePendingTransition(R.anim.no_animation_long, R.anim.fade_out);
        }
    }

    private static void doAnimFromBottomOnOver(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.slide_bottom_in, R.anim.no_animation);
        } else {
            activity.overridePendingTransition(0, R.anim.slide_bottom_out);
        }
    }

    private static void doAnimFromBottomPushOut(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.slide_bottom_in, R.anim.slide_top_out);
        } else {
            activity.overridePendingTransition(R.anim.slide_top_in, R.anim.slide_bottom_out);
        }
    }

    private static void doGrowFromBottom(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.grow_from_bottom_in, R.anim.no_animation);
        } else {
            activity.overridePendingTransition(R.anim.no_animation, R.anim.grow_from_bottom_out);
        }
    }

    private static void doJumpScale(Activity activity, boolean enter) {
        if (enter) {
            activity.overridePendingTransition(R.anim.no_animation_for_jump, R.anim.activity_anim_jump);
        } else {
            doAnimFade(activity, false);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, FADE_IN, FROM_BOTTOM_ON_OVER,
            SLIDE_LEFT_IN, LIKE_VINE, LEFT_IN, GROW_BOTTOM, FROM_BOTTOM_PUSH_OUT, JUMP_SCALE})

    public @interface AnimationType {
    }
}
