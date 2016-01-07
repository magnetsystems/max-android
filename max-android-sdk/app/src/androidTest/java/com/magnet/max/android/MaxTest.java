/**
 * Copyright (c) 2012-2016 Magnet Systems. All rights reserved.
 */
package com.magnet.max.android;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import com.magnet.max.android.Max;
import com.magnet.max.android.config.MaxAndroidConfig;
import com.magnet.max.android.config.MaxAndroidPropertiesConfig;

public class MaxTest extends AndroidTestCase {

  @MediumTest
  public void testInit() {
    Max.init(getContext(), new MaxAndroidPropertiesConfig(getContext(), com.magnet.max.android.tests.R.raw.testapp));
  }

  @MediumTest
  public void testInitCalledTwice() {
    MaxAndroidConfig config = new MaxAndroidPropertiesConfig(getContext(), com.magnet.max.android.tests.R.raw.testapp);
    Max.init(getContext(), config);
    Max.init(getContext(), config);
  }
}
