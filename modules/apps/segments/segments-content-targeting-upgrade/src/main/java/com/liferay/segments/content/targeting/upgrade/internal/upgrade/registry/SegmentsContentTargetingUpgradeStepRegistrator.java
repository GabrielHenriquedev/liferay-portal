/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.segments.content.targeting.upgrade.internal.upgrade.registry;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;
import com.liferay.segments.content.targeting.upgrade.internal.upgrade.v1_0_0.ContentTargetingUpgradeProcess;
import com.liferay.segments.content.targeting.upgrade.internal.upgrade.v1_0_0.util.RuleConverter;
import com.liferay.segments.service.SegmentsEntryLocalService;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Eduardo García
 */
@Component(service = UpgradeStepRegistrator.class)
public class SegmentsContentTargetingUpgradeStepRegistrator
	implements UpgradeStepRegistrator {

	public RuleConverter getRuleConverter(String ruleKey) {
		return _serviceTrackerMap.getService(ruleKey);
	}

	@Override
	public void register(Registry registry) {
		registry.registerInitialization();

		registry.register(
			"0.0.1", "1.0.0",
			new ContentTargetingUpgradeProcess(
				_segmentsEntryLocalService, this));
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, RuleConverter.class, "rule.converter.key");
	}

	@Reference
	private SegmentsEntryLocalService _segmentsEntryLocalService;

	private ServiceTrackerMap<String, RuleConverter> _serviceTrackerMap;

}