package dynoapps.exchange_rates.ui.widget.recyclerview;

/**
 * Created by erdemmac on 14/12/2016.
 */


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Abstract base class for {@link RecyclerView.Adapter}s whose data can be updated.
 */
public abstract class UpdatableAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    public abstract void update(@NonNull T updatedData);

}
