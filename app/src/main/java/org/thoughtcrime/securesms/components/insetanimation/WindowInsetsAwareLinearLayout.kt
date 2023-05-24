package org.thoughtcrime.securesms.components.insetanimation

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.InputAwareLayout.InputView
import org.thoughtcrime.securesms.components.KeyboardAwareLinearLayout.OnKeyboardHiddenListener
import org.thoughtcrime.securesms.components.KeyboardAwareLinearLayout.OnKeyboardShownListener
import org.thoughtcrime.securesms.util.ServiceUtil

/**
 * LinearLayout that shifts its children up when the IME is shown.
 *
 * Heavily dependent on [RootViewDeferringInsetsCallback] and [TranslateDeferringInsetsAnimationCallback]
 * which were lifted from https://github.com/android/user-interface-samples/tree/main/WindowInsetsAnimation
 */
class WindowInsetsAwareLinearLayout(
  context: Context,
  attrs: AttributeSet
) : LinearLayoutCompat(context, attrs), IInputAwareLayout {

  private val hiddenListeners: MutableSet<OnKeyboardHiddenListener> = mutableSetOf()
  private val shownListeners: MutableSet<OnKeyboardShownListener> = mutableSetOf()

  init {
    clipToPadding = false
    val deferringInsetsListener = RootViewDeferringInsetsCallback(
      persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
      deferredInsetTypes = WindowInsetsCompat.Type.ime()
    )
    ViewCompat.setWindowInsetsAnimationCallback(this, deferringInsetsListener)

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
      if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
        shownListeners.forEach { it.onKeyboardShown() }
      } else {
        hiddenListeners.forEach { it.onKeyboardHidden() }
      }
      deferringInsetsListener.onApplyWindowInsets(v, insets)
    }
//    ViewCompat.setOnApplyWindowInsetsListener(this, deferringInsetsListener)

    doOnPreDraw {
      children.forEach {
        ViewCompat.setWindowInsetsAnimationCallback(
          it,
          TranslateDeferringInsetsAnimationCallback(
            view = it,
            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
            deferredInsetTypes = WindowInsetsCompat.Type.ime(),
            // We explicitly allow dispatch to continue down to binding.messageHolder's
            // child views, so that step 2.5 below receives the call
            dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
          )
        )
      }
    }

  }

  override fun show(imeTarget: EditText, input: InputView) {
    if (isKeyboardOpen()) {
      hideSoftkey(imeTarget) {
        hideAttachedInput(true)
        input.show(getKeyboardHeight(), true)
        currentInput = input
      }

    } else {
      currentInput?.hide(true)
      input.show(getKeyboardHeight(), currentInput != null)
      currentInput = input
    }
  }

  override var currentInput: InputView? = null

  override fun hideCurrentInput(imeTarget: EditText) {
    if (isKeyboardOpen()) hideSoftkey(imeTarget, null) else hideAttachedInput(false)
  }

  override fun hideAttachedInput(instant: Boolean) {
    currentInput?.hide(instant)
    currentInput = null
  }

  override val isInputOpen = false

  override fun showSoftkey(inputTarget: EditText) {
    ServiceUtil.getInputMethodManager(inputTarget.context)
      .showSoftInput(inputTarget, 0)
  }

  private fun postOnKeyboardClose(runnable: Runnable) {
    if (isKeyboardOpen()) {
      hiddenListeners.add(object : OnKeyboardHiddenListener {
        override fun onKeyboardHidden() {
          hiddenListeners.remove(this)
          runnable.run()
        }
      })
    } else {
      runnable.run()
    }
  }

  override fun hideSoftkey(inputTarget: EditText, runAfterClose: Runnable?) {
    runAfterClose?.let { postOnKeyboardClose(it) }
    ServiceUtil.getInputMethodManager(inputTarget.context)
      .hideSoftInputFromWindow(inputTarget.windowToken, 0)
  }

  override fun setIsBubble(isBubble: Boolean) {

  }

  override fun addOnKeyboardShownListener(listener: OnKeyboardShownListener) {
    shownListeners.add(listener)
  }

  override fun isKeyboardOpen(): Boolean {
    return ViewCompat.getRootWindowInsets(this)?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
  }

  override fun getKeyboardHeight(): Int {
    val height = ViewCompat.getRootWindowInsets(this)?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
    return if (height == 0) {
      resources.getDimensionPixelSize(R.dimen.default_custom_keyboard_size)
    } else {
      height
    }
  }

}