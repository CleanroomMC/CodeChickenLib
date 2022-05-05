package codechicken.lib.configv3;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

import static net.covers1624.quack.util.SneakyUtils.unsafeCast;

/**
 * Created by covers1624 on 17/4/22.
 */
public abstract class AbstractConfigTag<T extends ConfigTag> implements ConfigTag {

    private final String name;
    @Nullable
    private final ConfigCategory parent;
    protected boolean dirty;
    protected List<String> comment = List.of();
    private final List<ConfigCallback<T>> onModifiedCallbacks = new LinkedList<>();

    protected AbstractConfigTag(String name, @Nullable ConfigCategory parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Nullable
    @Override
    public final ConfigCategory getParent() {
        return parent;
    }

    @Override
    public T setComment(String comment) {
        return setComment(comment.split("\n"));
    }

    @Override
    public T setComment(String... comment) {
        return setComment(List.of(comment));
    }

    @Override
    public T setComment(List<String> comment) {
        this.comment = List.copyOf(comment);
        dirty = true;
        return unsafeCast(this);
    }

    @Override
    public final List<String> getComment() {
        return comment;
    }

    public T onSync(ConfigCallback<T> callback) {
        onModifiedCallbacks.add(callback);
        return unsafeCast(this);
    }

    @Override
    public void forceSync() {
        runSync(ConfigCallback.Reason.MANUAL);
    }

    public void runSync(ConfigCallback.Reason reason) {
        for (ConfigCallback<T> callback : onModifiedCallbacks) {
            callback.onSync(unsafeCast(this), reason);
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Internal
    public void clearDirty() {
        dirty = false;
    }
}
