package com.liferay.portal.workflow.kaleo.internal.upgrade.v3_5_1;

import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.UpgradeProcessFactory;
import com.liferay.portal.kernel.upgrade.UpgradeStep;

/**
 * @author Gabriel Santos
 */
public class KaleoActionUpgradeProcess  extends UpgradeProcess{

	@Override
	protected void doUpgrade() throws Exception {
		runSQL(
			"UPDATE KaleoAction SET type_ = 'SCRIPT'");
	}

	@Override
	protected UpgradeStep[] getPreUpgradeSteps() {
		return new UpgradeStep[] {
			UpgradeProcessFactory.addColumns(
				"KaleoAction", "status INTEGER", "type_ VARCHAR(75)")
		};
	}
}
