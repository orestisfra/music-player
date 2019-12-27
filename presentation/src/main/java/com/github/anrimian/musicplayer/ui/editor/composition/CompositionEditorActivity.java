package com.github.anrimian.musicplayer.ui.editor.composition;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.editor.composition.list.ShortGenresAdapter;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_ARTIST_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.AUTHOR_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.FILE_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.GENRE_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.TITLE_TAG;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.makeSnackbar;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setStatusBarColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class CompositionEditorActivity extends MvpAppCompatActivity
        implements CompositionEditorView {

    @InjectPresenter
    CompositionEditorPresenter presenter;

    @BindView(R.id.container)
    View container;

    @BindView(R.id.tv_author)
    TextView tvAuthor;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.tv_album)
    TextView tvAlbum;

    @BindView(R.id.tv_album_artist)
    TextView tvAlbumArtist;

    @BindView(R.id.tv_filename)
    TextView tvFileName;

    @BindView(R.id.rv_genres)
    RecyclerView rvGenres;

    @BindView(R.id.tv_author_hint)
    TextView tvAuthorHint;

    @BindView(R.id.tv_title_hint)
    TextView tvTitleHint;

    @BindView(R.id.tv_album_hint)
    TextView tvAlbumHint;

    @BindView(R.id.tv_album_author_hint)
    TextView tvAlbumArtistHint;

    @BindView(R.id.tv_filename_hint)
    TextView tvFileNameHint;

    @BindView(R.id.tv_genre_hint)
    TextView tvGenreHint;

    @BindView(R.id.change_author_clickable_area)
    View changeAuthorClickableArea;

    @BindView(R.id.change_title_clickable_area)
    View changeTitleClickableArea;

    @BindView(R.id.change_filename_clickable_area)
    View changeFilenameClickableArea;

    @BindView(R.id.change_album_clickable_area)
    View changeAlbumClickableArea;

    @BindView(R.id.change_album_artist_clickable_area)
    View changeAlbumArtistClickableArea;

    @BindView(R.id.change_genre_clickable_area)
    View changeGenreClickableArea;

    @BindView(R.id.iv_album_artist)
    View ivAlbumArtist;

    @BindView(R.id.divider_album_artist)
    View dividerAlbumArtist;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private DialogFragmentRunner<InputTextDialogFragment> authorDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> titleDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> filenameDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> albumDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> albumArtistDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> genreDialogFragmentRunner;

    private ShortGenresAdapter genresAdapter;

    public static Intent newIntent(Context context, long compositionId) {
        Intent intent = new Intent(context, CompositionEditorActivity.class);
        intent.putExtra(COMPOSITION_ID_ARG, compositionId);
        return intent;
    }

    @ProvidePresenter
    CompositionEditorPresenter providePresenter() {
        long compositionId = getIntent().getLongExtra(COMPOSITION_ID_ARG, 0);
        return Components.getCompositionEditorComponent(compositionId).compositionEditorPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composition_edit);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.edit_tags);
        }

        genresAdapter = new ShortGenresAdapter(rvGenres);
        rvGenres.setAdapter(genresAdapter);
        rvGenres.setLayoutManager(ChipsLayoutManager.newBuilder(this).build());

        changeAuthorClickableArea.setOnClickListener(v -> presenter.onChangeAuthorClicked());
        changeTitleClickableArea.setOnClickListener(v -> presenter.onChangeTitleClicked());
        changeFilenameClickableArea.setOnClickListener(v -> presenter.onChangeFileNameClicked());
        changeAlbumClickableArea.setOnClickListener(v -> presenter.onChangeAlbumClicked());
        changeAlbumArtistClickableArea.setOnClickListener(v -> presenter.onChangeAlbumArtistClicked());
        changeGenreClickableArea.setOnClickListener(v -> presenter.onChangeGenreClicked());
        onLongClick(changeAuthorClickableArea, () -> copyText(tvAuthor, tvAuthorHint));
        onLongClick(changeTitleClickableArea, () -> copyText(tvTitle, tvTitleHint));
        onLongClick(changeFilenameClickableArea, presenter::onCopyFileNameClicked);
        onLongClick(changeAlbumClickableArea, () -> copyText(tvAlbum, tvAlbumHint));
        onLongClick(changeAlbumArtistClickableArea, () -> copyText(tvAlbumArtist, tvAlbumArtistHint));
//        onLongClick(changeGenreClickableArea, () -> copyText(tvGenre, tvGenreHint));

        @ColorInt int statusBarColor = getColorFromAttr(this, R.attr.colorPrimaryDark);
        Slidr.attach(this, getWindow().getStatusBarColor(), statusBarColor);
        setStatusBarColor(getWindow(), statusBarColor);

        FragmentManager fm = getSupportFragmentManager();
        authorDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                AUTHOR_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAuthorEntered));

        titleDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                TITLE_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewTitleEntered));

        filenameDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                FILE_NAME_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewFileNameEntered));

        albumDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                ALBUM_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAlbumEntered));

        albumArtistDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                ALBUM_ARTIST_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAlbumArtistEntered));

        genreDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                GENRE_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewGenreEntered));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void showCompositionLoadingError(ErrorCommand errorCommand) {
        tvAuthor.setText(errorCommand.getMessage());
    }

    @Override
    public void showComposition(FullComposition composition) {
        tvTitle.setText(composition.getTitle());

        String album = composition.getAlbum();
        tvAlbum.setText(album);
        int albumArtistVisibility = album == null? GONE : VISIBLE;
        tvAlbumArtist.setVisibility(albumArtistVisibility);
        tvAlbumArtistHint.setVisibility(albumArtistVisibility);
        ivAlbumArtist.setVisibility(albumArtistVisibility);
        dividerAlbumArtist.setVisibility(albumArtistVisibility);
        tvAlbumArtist.setText(composition.getAlbumArtist());

        tvAuthor.setText(formatAuthor(composition.getArtist(), this));
        tvFileName.setText(formatFileName(composition.getFilePath(), true));
    }

    @Override
    public void showGenres(List<ShortGenre> shortGenres) {
//        FlowLa
//        FlowLayout
//        StringBuilder sb = new StringBuilder();
//        for (ShortGenre genre: shortGenres) {
//            sb.append(genre.getName());
//            sb.append("; ");
//        }
        genresAdapter.submitList(shortGenres);
//        tvGenre.setText(sb.toString());
    }

    @Override
    public void showEnterAuthorDialog(FullComposition composition, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_author_name,
                R.string.change,
                R.string.cancel,
                R.string.artist,
                composition.getArtist())
                .hints(hints)
                .build();
        authorDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterAlbumArtistDialog(FullComposition composition, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
                R.string.change_album_artist,
                R.string.change,
                R.string.cancel,
                R.string.artist,
                composition.getAlbumArtist())
                .hints(hints)
                .build();
        albumArtistDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterGenreDialog(FullComposition composition, String[] genres) {
//        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(
//                R.string.change_composition_genre,
//                R.string.change,
//                R.string.cancel,
//                R.string.genre,
//                composition.getGenre())
//                .hints(genres)
//                .build();
//        genreDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterTitleDialog(FullComposition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_title,
                R.string.change,
                R.string.cancel,
                R.string.title,
                composition.getTitle());
        titleDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterFileNameDialog(FullComposition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_file_name,
                R.string.change,
                R.string.cancel,
                R.string.filename,
                formatFileName(composition.getFilePath()),
                false);
        filenameDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterAlbumDialog(FullComposition composition, String[] hints) {
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(R.string.change_album_name,
                R.string.change,
                R.string.cancel,
                R.string.album,
                composition.getAlbum())
                .hints(hints)
                .build();
        albumDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        makeSnackbar(container, errorCommand.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void copyFileNameText(String filePath) {
        AndroidUtils.copyText(this,
                formatFileName(filePath),
                getString(R.string.filename));
        onTextCopied();
    }

    private void copyText(TextView textView, TextView tvLabel) {
        AndroidUtils.copyText(this,
                textView.getText().toString(),
                tvLabel.getText().toString());
        onTextCopied();
    }

    private void onTextCopied() {
        makeSnackbar(container, R.string.copied_message, Snackbar.LENGTH_SHORT).show();
    }
}
