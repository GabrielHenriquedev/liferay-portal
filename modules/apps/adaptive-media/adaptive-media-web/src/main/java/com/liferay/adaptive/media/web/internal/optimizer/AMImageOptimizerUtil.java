/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.adaptive.media.web.internal.optimizer;

import com.liferay.adaptive.media.image.optimizer.AMImageOptimizer;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.concurrent.DCLSingleton;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.Collection;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Sergio González
 */
@Component(service = {})
public class AMImageOptimizerUtil {

	public static ServiceTrackerMap<String, AMImageOptimizer> _createServiceTrackerMap() {

		ServiceTrackerMap<String, AMImageOptimizer>
			serviceTrackerMap =
			new ServiceTrackerMap<String, AMImageOptimizer>() {
				@Override
				public void close() {
					_serviceTrackerMapDCLSingleton.destroy(ServiceTrackerMap::close);
				}

				@Override
				public boolean containsKey(String key) {
					return false;
				}

				@Override
				public AMImageOptimizer getService(String key) {
					return null;
				}

				@Override
				public Set<String> keySet() {
					return null;
				}

				@Override
				public Collection<AMImageOptimizer> values() {
					return null;
				}

			};

		return serviceTrackerMap;
	}
	public static void optimize(long companyId) {
		ServiceTrackerMap<String, AMImageOptimizer> serviceTrackerMap = _serviceTrackerMapDCLSingleton.getSingleton(AMImageOptimizerUtil::_createServiceTrackerMap);
		if (serviceTrackerMap == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to optimize for company " + companyId +
						" because the component is not active");
			}

			return;
		}

		Set<String> modelClassNames = serviceTrackerMap.keySet();

		for (String modelClassName : modelClassNames) {
			AMImageOptimizer amImageOptimizer = serviceTrackerMap.getService(
				modelClassName);

			amImageOptimizer.optimize(companyId);
		}
	}

	public static void optimize(long companyId, String configurationEntryUuid) {
		ServiceTrackerMap<String, AMImageOptimizer> serviceTrackerMap = _serviceTrackerMapDCLSingleton.getSingleton(AMImageOptimizerUtil::_createServiceTrackerMap);
		if (serviceTrackerMap == null) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to optimize for company " + companyId +
						" because the component is not active");
			}

			return;
		}

		Set<String> modelClassNames = serviceTrackerMap.keySet();

		for (String modelClassName : modelClassNames) {
			AMImageOptimizer amImageOptimizer = serviceTrackerMap.getService(
				modelClassName);

			amImageOptimizer.optimize(companyId, configurationEntryUuid);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		ServiceTrackerMap<String, AMImageOptimizer> serviceTrackerMap = _serviceTrackerMapDCLSingleton.getSingleton(AMImageOptimizerUtil::_createServiceTrackerMap);
		serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, AMImageOptimizer.class, "adaptive.media.key");
	}


	private static final Log _log = LogFactoryUtil.getLog(
		AMImageOptimizerUtil.class);

	public static final DCLSingleton
		<ServiceTrackerMap<String, AMImageOptimizer>>
		_serviceTrackerMapDCLSingleton = new DCLSingleton<>();

	private static ServiceTrackerMap<String, AMImageOptimizer>
		_serviceTrackerMap;

}