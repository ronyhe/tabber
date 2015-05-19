package tabber;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import java.util.Iterator;

/**
 * This class enables keyboard cycling between actors.
 * It simulates the TAB key scrolling that's available in web forms and other 2D UIs.
 * Usage example:
 * <pre>{@code
 * List<Actor> actorsToCycleThrough = Arrays.asList(actor1, actor2, actor3);
 * Tabber tabber = new Tabber(actorsToCycleThrough, Tabber.DEBUG_HIGHLIGHTER);
 * InputMultiPlexer plexer = new InputMultiPlexer(tabber, stage);
 * Gdx.input.setInputProcessor(plexer);
 * }</pre>
 */
public class Tabber extends InputAdapter{

    /**
     * Used to visualize the selection of the currently selected actor.
     * Every time tha TAB key is clicked:
     * unhighlight will be called on the previous actor.
     * highlight will be called in the current actor.
     */
    public interface Highlighter {
        void highlight(Actor actor);
        void unhighlight(Actor actor);
    }

    /**
     * A utility Highlighter that uses Actor#setDebug(true) and Actor#setDebug(false)
     * to highlight and unhighlight actors respectively.
     */
    public static final Highlighter DEBUG_HIGHLIGHTER = new DebugHighlighter();

    private static final int CYCLE_KEY_CODE = Input.Keys.TAB;
    private static final int CLICK_KEY_CODE = Input.Keys.ENTER;

    private final Highlighter highlighter;

    // Eliminate the need for null checks by initializing the currentActor to a special case object
    private Actor currentActor = new InitialDummyActor();
    private CircularIterator<Actor> actors;

    // Simple events for simulating a click on an actor
    // Defined as fields to avoid creating unneeded instances
    private final Event touchDownEvent = new TouchDownEvent();
    private final Event touchUpEvent = new TouchUpEvent();


    public Tabber(Iterable<? extends Actor> actorsToCycleThrough, Highlighter highlighter) {
        this.actors = new CircularIterator<>(actorsToCycleThrough);
        this.highlighter = highlighter;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (cycleKeyWasClicked(keycode)) {
            switchToNextActor();
        }
        if (clickKeyWasClicked(keycode)) {
            simulateClickOnCurrentActor();
        }
        return super.keyUp(keycode);
    }

    private void switchToNextActor() {
        highlighter.unhighlight(currentActor);
        currentActor = getNextActor();
        highlighter.highlight(currentActor);
    }

    private void simulateClickOnCurrentActor() {
        currentActor.fire(touchDownEvent);
        currentActor.fire(touchUpEvent);
    }

    private Actor getNextActor() {
        return actors.next();
    }

    private boolean clickKeyWasClicked(int keycode) {
        return keycode == CLICK_KEY_CODE;
    }

    private boolean cycleKeyWasClicked(int keycode) {
        return keycode == CYCLE_KEY_CODE;
    }

    private static class TouchDownEvent extends InputEvent {
        public TouchDownEvent() {
            setType(Type.touchDown);
        }
    }

    private static class TouchUpEvent extends InputEvent {
        public TouchUpEvent() {
            setType(Type.touchUp);
        }
    }

    private static class InitialDummyActor extends Actor {}

    private static class DebugHighlighter implements Highlighter {

        @Override
        public void highlight(Actor actor) {
            actor.setDebug(true);
        }

        @Override
        public void unhighlight(Actor actor) {
            actor.setDebug(false);
        }
    }

    private static class CircularIterator<T> {

        private Iterable<? extends T> iterable;
        private Iterator<? extends T> iterator;

        private CircularIterator(Iterable<? extends T> iterable) {
            this.iterable = iterable;
            this.iterator = iterable.iterator();
        }

        private T next() {
            if (!iterator.hasNext()) {
                iterator = iterable.iterator();
            }
            return iterator.next();
        }
    }

}