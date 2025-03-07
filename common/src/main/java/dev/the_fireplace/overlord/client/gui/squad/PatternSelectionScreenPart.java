package dev.the_fireplace.overlord.client.gui.squad;

import dev.the_fireplace.overlord.OverlordConstants;
import dev.the_fireplace.overlord.client.gui.PartialScreen;
import dev.the_fireplace.overlord.domain.data.objects.Pattern;
import dev.the_fireplace.overlord.domain.registry.PatternRegistry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class PatternSelectionScreenPart implements PartialScreen
{
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final State state;

    private final PatternRegistry patternRegistry;
    private final List<PatternButtonWidget> patternWidgets = new ArrayList<>();
    private Button previousButton;
    private Button nextButton;

    public PatternSelectionScreenPart(int x, int y, int width, int height, State state) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.state = state;
        this.patternRegistry = OverlordConstants.getInjector().getInstance(PatternRegistry.class);
        createWidgets();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GuiEventListener & Widget & NarratableEntry> List<T> getChildren() {
        List<T> children = new ArrayList<>(patternWidgets.size() + 2);
        children.addAll((Collection<? extends T>) patternWidgets);
        children.add((T) nextButton);
        children.add((T) previousButton);

        return children;
    }

    private void createWidgets() {
        createPatternWidgets();
        calculateStartingPage();
        updatePatternVisibility();
        createPageChangeButtons();
    }

    private void createPatternWidgets() {
        patternWidgets.clear();
        int columnIndex = 0;
        int rowIndex = 0;
        int widgetWidth = getPatternWidgetWidth();
        int widgetHeight = getPatternWidgetHeight();
        int displayedColumnCount = getColumnCount();
        int displayedRowCount = getRowCount();
        for (Pattern pattern : patternRegistry.getPatterns()) {
            int column = columnIndex % displayedColumnCount;
            int row = rowIndex % displayedRowCount;
            int widgetX = x + (column * (widgetWidth + 4));
            int widgetY = y + 24 + (row * (widgetHeight + 4));
            ResourceLocation patternId = pattern.getId();
            Component widgetName = new TranslatableComponent("squad." + patternId.getNamespace() + ".pattern." + patternId.getPath() + ".name");
            PatternButtonWidget patternButtonWidget = new PatternButtonWidget(
                widgetX,
                widgetY,
                widgetWidth,
                widgetHeight,
                widgetName,
                patternId,
                patternWidget -> {
                    this.state.setPatternId(patternId);
                    notifyChildrenOfPattern();
                }
            );
            this.patternWidgets.add(patternButtonWidget);
            columnIndex++;
            if (columnIndex % displayedColumnCount == 0) {
                rowIndex++;
            }
        }
        notifyChildrenOfPattern();
    }

    private int getPatternAreaHeight() {
        return this.height - 24;
    }

    private int getPatternAreaWidth() {
        return this.width;
    }

    private int getPatternWidgetHeight() {
        return getPatternAreaHeight() / getRowCount() - 4;
    }

    private int getPatternWidgetWidth() {
        return getPatternAreaWidth() / getColumnCount() - 4;
    }

    private int getColumnCount() {
        int minimumPatternWidth = 70;
        return Math.max(1, getPatternAreaWidth() / minimumPatternWidth);
    }

    private int getRowCount() {
        int minimumPatternHeight = 70;
        return Math.max(1, getPatternAreaHeight() / minimumPatternHeight);
    }

    private int getPatternsPerPage() {
        return getColumnCount() * getRowCount();
    }

    private void updatePatternVisibility() {
        for (int index = 0; index < patternWidgets.size(); index++) {
            int patternPage = index / getPatternsPerPage();
            patternWidgets.get(index).visible = patternPage == this.state.currentPage;
        }
    }

    private int getPageCount() {
        int patternCount = patternWidgets.size();
        int patternsPerPage = getPatternsPerPage();
        return patternCount / patternsPerPage + (patternCount % patternsPerPage == 0 ? 0 : 1);
    }

    private void calculateStartingPage() {
        if (this.state.patternId.getPath().isEmpty()) {
            this.state.currentPage = 0;
            return;
        }
        int widgetIndex = 0;
        for (PatternButtonWidget patternWidget : this.patternWidgets) {
            if (patternWidget.patternId.equals(this.state.patternId)) {
                this.state.currentPage = (byte) (widgetIndex / getPatternsPerPage());
                return;
            }
            widgetIndex++;
        }
        this.state.currentPage = 0;
    }

    private void createPageChangeButtons() {
        previousButton = new Button(x, y, this.width / 2 - 2, 20, new TranslatableComponent("gui.overlord.create_squad.previous"), buttonWidget -> {
            this.state.currentPage--;
            updatePageChangeButtonUsability();
            updatePatternVisibility();
        });
        nextButton = new Button(x + width / 2 + 4, y, this.width / 2 - 6, 20, new TranslatableComponent("gui.overlord.create_squad.next"), buttonWidget -> {
            this.state.currentPage++;
            updatePageChangeButtonUsability();
            updatePatternVisibility();
        });
        updatePageChangeButtonUsability();
    }

    private void updatePageChangeButtonUsability() {
        this.previousButton.active = this.state.currentPage > 0;
        this.nextButton.active = this.state.currentPage < getPageCount() - 1;
    }

    private void notifyChildrenOfPattern() {
        for (PatternButtonWidget patternWidget : patternWidgets) {
            patternWidget.notifyOfActivePattern(this.state.patternId);
        }
    }

    static class State
    {
        private final Consumer<ResourceLocation> onChanged;
        private byte currentPage = 0;
        private ResourceLocation patternId;

        public State(ResourceLocation patternId, Consumer<ResourceLocation> onChanged) {
            this.patternId = patternId;
            this.onChanged = onChanged;
        }

        private void setPatternId(ResourceLocation patternId) {
            this.patternId = patternId;
            onChanged.accept(patternId);
        }

        public ResourceLocation getPatternId() {
            return patternId;
        }
    }
}
