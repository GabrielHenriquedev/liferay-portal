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

package com.liferay.segments.web.internal.util;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.segments.constants.SegmentsEntryConstants;
import com.liferay.segments.model.SegmentsEntry;
import com.liferay.segments.source.provider.SegmentsSourceDetailsProvider;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author Alejandro Tardín
 */
public class SegmentsSourceDetailsProviderUtil {

	public static SegmentsSourceDetailsProvider
		getSegmentsSourceDetailsProvider(SegmentsEntry segmentsEntry) {

		SegmentsSourceDetailsProvider segmentsSourceDetailsProvider =
			_serviceTrackerMap.getService(segmentsEntry.getSource());

		if (segmentsSourceDetailsProvider != null) {
			return segmentsSourceDetailsProvider;
		}

		return _serviceTrackerMap.getService(
			SegmentsEntryConstants.SOURCE_DEFAULT);
	}

	private static final ServiceTrackerMap
		<String, SegmentsSourceDetailsProvider> _serviceTrackerMap;

	static {
		Bundle bundle = FrameworkUtil.getBundle(
			SegmentsSourceDetailsProviderUtil.class);

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundle.getBundleContext(), SegmentsSourceDetailsProvider.class,
			"segments.source");
	}

}