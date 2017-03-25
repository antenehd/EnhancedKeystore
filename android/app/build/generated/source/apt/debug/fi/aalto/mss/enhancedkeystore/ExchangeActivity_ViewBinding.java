// Generated code from Butter Knife. Do not modify!
package fi.aalto.mss.enhancedkeystore;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ExchangeActivity_ViewBinding implements Unbinder {
  private ExchangeActivity target;

  @UiThread
  public ExchangeActivity_ViewBinding(ExchangeActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public ExchangeActivity_ViewBinding(ExchangeActivity target, View source) {
    this.target = target;

    target.mStatusTextView = Utils.findRequiredViewAsType(source, R.id.bt_status_value_tv, "field 'mStatusTextView'", TextView.class);
    target.mConnectButton = Utils.findRequiredViewAsType(source, R.id.bt_connect_btn, "field 'mConnectButton'", Button.class);
    target.mExchangeInitButton = Utils.findRequiredViewAsType(source, R.id.bt_init_exchange_btn, "field 'mExchangeInitButton'", Button.class);
    target.mKeyConfirmationTextView = Utils.findRequiredViewAsType(source, R.id.tv_key_confirmation, "field 'mKeyConfirmationTextView'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ExchangeActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mStatusTextView = null;
    target.mConnectButton = null;
    target.mExchangeInitButton = null;
    target.mKeyConfirmationTextView = null;
  }
}
