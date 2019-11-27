package com.github.anrimian.musicplayer.ui.editor;

import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.SingleStateByTagStrategy;

import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.SkipStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface CompositionEditorView extends MvpView {

    String DISPLAY_COMPOSITION_STATE = "display_composition_state";

    @StateStrategyType(OneExecutionStateStrategy.class)
    void closeScreen();

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showCompositionLoadingError(ErrorCommand errorCommand);

    @StateStrategyType(value = SingleStateByTagStrategy.class, tag = DISPLAY_COMPOSITION_STATE)
    void showComposition(FullComposition composition);

    @StateStrategyType(OneExecutionStateStrategy.class)
    void showErrorMessage(ErrorCommand errorCommand);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAuthorDialog(FullComposition composition, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterTitleDialog(FullComposition composition);

    @StateStrategyType(SkipStrategy.class)
    void showEnterFileNameDialog(FullComposition composition);

    @StateStrategyType(SkipStrategy.class)
    void copyFileNameText(String filePath);

    @StateStrategyType(SkipStrategy.class)
    void showEnterAlbumDialog(FullComposition composition, String[] hints);

    @StateStrategyType(SkipStrategy.class)
    void showEnterGenreDialog(FullComposition composition, String[] genres);
}
