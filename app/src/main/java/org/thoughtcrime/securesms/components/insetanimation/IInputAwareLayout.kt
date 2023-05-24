/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.thoughtcrime.securesms.components.insetanimation

import android.widget.EditText
import org.thoughtcrime.securesms.components.InputAwareLayout.InputView
import org.thoughtcrime.securesms.components.KeyboardAwareLinearLayout.OnKeyboardShownListener

interface IInputAwareLayout {
  fun show(imeTarget: EditText, input: InputView)

  val currentInput: InputView?

  fun hideCurrentInput(imeTarget: EditText)

  fun hideAttachedInput(instant: Boolean)

  val isInputOpen: Boolean

  fun showSoftkey(inputTarget: EditText)

  fun hideSoftkey(inputTarget: EditText, runAfterClose: Runnable?)

  fun setIsBubble(isBubble: Boolean)

  fun addOnKeyboardShownListener(listener: OnKeyboardShownListener)

  fun isKeyboardOpen(): Boolean

  fun getKeyboardHeight(): Int

}