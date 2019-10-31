package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;

/**
 * Created on 31.10.2017.
 */

public class CompositionsAdapter extends DiffListAdapter<Composition, MusicViewHolder> {

    private final Set<MusicViewHolder> viewHolders = new HashSet<>();

    private final HashSet<Composition> selectedCompositions;
    private OnPositionItemClickListener<Composition> onCompositionClickListener;
    private OnPositionItemClickListener<Composition> onLongClickListener;
    private OnPositionItemClickListener<Composition> iconClickListener;

    @Nullable
    private Composition currentComposition;
    private boolean play;
    private boolean isCoversEnabled;

    public CompositionsAdapter(RecyclerView recyclerView,
                               HashSet<Composition> selectedCompositions) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                CompositionHelper::areSourcesTheSame,
                CompositionHelper::getChangePayload)
        );
        this.selectedCompositions = selectedCompositions;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(parent,
                onCompositionClickListener,
                onLongClickListener,
                iconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        viewHolders.add(holder);

        Composition composition = getItem(position);
        holder.bind(composition, isCoversEnabled);
        boolean selected = selectedCompositions.contains(composition);
        holder.setSelected(selected);

        boolean isCurrentComposition = composition.equals(currentComposition);
        holder.showAsCurrentComposition(isCurrentComposition);
        holder.showAsPlaying(isCurrentComposition && play);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        for (Object payload: payloads) {
            if (payload == ITEM_SELECTED) {
                holder.setSelected(true);
                return;
            }
            if (payload == ITEM_UNSELECTED) {
                holder.setSelected(false);
                return;
            }
        }
        holder.update(getItem(position), payloads);
    }

    @Override
    public void onViewRecycled(@NonNull MusicViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void setItemSelected(int position) {
        notifyItemChanged(position, ITEM_SELECTED);
    }

    public void setItemUnselected(int position) {
        notifyItemChanged(position, ITEM_UNSELECTED);
    }

    public void setItemsSelected(boolean selected) {
        for (MusicViewHolder holder: viewHolders) {
            holder.setSelected(selected);
        }
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnLongClickListener(OnPositionItemClickListener<Composition> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setIconClickListener(OnPositionItemClickListener<Composition> iconClickListener) {
        this.iconClickListener = iconClickListener;
    }

    public void showCurrentComposition(Composition currentComposition) {
        this.currentComposition = currentComposition;
        for (MusicViewHolder holder: viewHolders) {
            Composition composition = holder.getComposition();
            boolean isCurrentComposition = composition.equals(currentComposition);
            holder.showAsCurrentComposition(isCurrentComposition);
            holder.showAsPlaying(isCurrentComposition && play);
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (MusicViewHolder holder: viewHolders) {
            holder.setCoversVisible(isCoversEnabled);
        }
    }

    public void showPlaying(boolean play) {
        this.play = play;
        for (MusicViewHolder holder: viewHolders) {
            Composition composition = holder.getComposition();
            boolean isCurrentComposition = composition.equals(currentComposition);
            holder.showAsCurrentComposition(isCurrentComposition);
            holder.showAsPlaying(isCurrentComposition && play);
        }
    }
}
