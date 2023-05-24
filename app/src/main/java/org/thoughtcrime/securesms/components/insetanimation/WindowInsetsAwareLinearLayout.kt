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
import org.thoughtcrime.securesms.components.InputAwareLayout
import org.thoughtcrime.securesms.components.KeyboardAwareLinearLayout

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

    init {
        clipToPadding = false
        val deferringInsetsListener = RootViewDeferringInsetsCallback(
            persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
            deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )
        ViewCompat.setWindowInsetsAnimationCallback(this, deferringInsetsListener)
        ViewCompat.setOnApplyWindowInsetsListener(this, deferringInsetsListener)

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

  override fun show(imeTarget: EditText, input: InputAwareLayout.InputView) {

  }

  override val currentInput: InputAwareLayout.InputView? = null

  override fun hideCurrentInput(imeTarget: EditText) {

  }

  override fun hideAttachedInput(instant: Boolean) {

  }

  override val isInputOpen = false

  override fun showSoftkey(inputTarget: EditText) {

  }

  override fun hideSoftkey(inputTarget: EditText, runAfterClose: Runnable?) {

  }

  override fun setIsBubble(isBubble: Boolean) {

  }

  override fun addOnKeyboardShownListener(listener: KeyboardAwareLinearLayout.OnKeyboardShownListener) {

  }

  override fun isKeyboardOpen(): Boolean {
    return false
  }

  override fun getKeyboardHeight(): Int {
    return 0
  }

}