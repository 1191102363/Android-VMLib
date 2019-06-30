package me.shouheng.mvvm.base;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.shouheng.mvvm.annotation.ViewConfiguration;
import me.shouheng.mvvm.bus.EventBusManager;

import java.lang.reflect.ParameterizedType;

/**
 * The base common fragment implementation for MVVMs.
 *
 * @author WngShhng 2019-6-29
 */
public abstract class CommonFragment<T extends ViewDataBinding, U extends CommonViewModel> extends Fragment {

    private U vm;

    private T binding;

    private boolean shareViewModel = false;

    private boolean useEventBus = false;

    {
        ViewConfiguration configuration = this.getClass().getAnnotation(ViewConfiguration.class);
        if (configuration != null) {
            shareViewModel = configuration.shareViewMode();
            useEventBus = configuration.useEventBus();
        }
    }

    /**
     * Get the layout resource id from subclass.
     *
     * @return layout resource id.
     */
    protected abstract int getLayoutResId();

    /**
     * Do create view business.
     *
     * @param savedInstanceState the saved instance state.
     */
    protected abstract void doCreateView(@Nullable Bundle savedInstanceState);

    /**
     * Initialize view model according to the generic class type. Override this method to
     * add your owen implementation.
     *
     * Add {@link ViewConfiguration} to the subclass and set {@link ViewConfiguration#shareViewMode()} true
     * if you want to share view model between several fragments.
     *
     * @return the view model instance.
     */
    protected U initViewModel() {
        Class<U> vmClass = ((Class)((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
        if (shareViewModel) {
            return ViewModelProviders.of(getActivity()).get(vmClass);
        } else {
            return ViewModelProviders.of(this).get(vmClass);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getLayoutResId() <= 0) {
            throw new IllegalArgumentException("The subclass must provider a valid layout resources id.");
        }
        vm = initViewModel();
        binding = DataBindingUtil.inflate(getLayoutInflater(), getLayoutResId(), null, false);
        doCreateView(savedInstanceState);
        return binding.getRoot();
    }

    protected U getVM() {
        return vm;
    }

    protected T getBinding() {
        return binding;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (useEventBus) {
            EventBusManager.getInstance().register(this);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (useEventBus) {
            EventBusManager.getInstance().unregister(this);
        }
        super.onDestroy();
    }
}