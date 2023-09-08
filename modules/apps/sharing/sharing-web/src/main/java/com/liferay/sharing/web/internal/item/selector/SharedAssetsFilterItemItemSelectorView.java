/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.sharing.web.internal.item.selector;

import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.item.selector.ItemSelectorViewDescriptorRenderer;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.osgi.service.tracker.collections.map.PropertyServiceReferenceComparator;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.sharing.filter.SharedAssetsFilterItem;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	property = "item.selector.view.order:Integer=100",
	service = ItemSelectorView.class
)
public class SharedAssetsFilterItemItemSelectorView
	implements ItemSelectorView<SharedAssetsFilterItemItemSelectorCriterion> {

	public static SharedAssetsFilterItem getSharedAssetsFilterItem(
		String className) {

		for (SharedAssetsFilterItem sharedAssetsFilterItem :
				_serviceTrackerList) {

			if (StringUtil.equals(
					className, sharedAssetsFilterItem.getClassName())) {

				return sharedAssetsFilterItem;
			}
		}

		return null;
	}

	@Override
	public Class<SharedAssetsFilterItemItemSelectorCriterion>
		getItemSelectorCriterionClass() {

		return SharedAssetsFilterItemItemSelectorCriterion.class;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		return _language.get(locale, "asset-types");
	}

	@Override
	public void renderHTML(
			ServletRequest servletRequest, ServletResponse servletResponse,
			SharedAssetsFilterItemItemSelectorCriterion
				sharedAssetsFilterItemItemSelectorCriterion,
			PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		_itemSelectorViewDescriptorRenderer.renderHTML(
			servletRequest, servletResponse,
			sharedAssetsFilterItemItemSelectorCriterion, portletURL,
			itemSelectedEventName, search,
			new ItemSelectorViewDescriptor<SharedAssetsFilterItem>() {

				@Override
				public ItemDescriptor getItemDescriptor(
					SharedAssetsFilterItem sharedAssetsFilterItem) {

					return new ItemDescriptor() {

						@Override
						public String getIcon() {
							return sharedAssetsFilterItem.getIcon();
						}

						@Override
						public String getImageURL() {
							return null;
						}

						@Override
						public String getPayload() {
							return sharedAssetsFilterItem.getClassName();
						}

						@Override
						public String getSubtitle(Locale locale) {
							return null;
						}

						@Override
						public String getTitle(Locale locale) {
							return sharedAssetsFilterItem.getLabel(locale);
						}

						@Override
						public boolean isCompact() {
							return true;
						}

					};
				}

				@Override
				public ItemSelectorReturnType getItemSelectorReturnType() {
					return new SharedAssetsFilterItemItemSelectorReturnType();
				}

				@Override
				public SearchContainer<SharedAssetsFilterItem>
					getSearchContainer() {

					SearchContainer<SharedAssetsFilterItem>
						entriesSearchContainer = new SearchContainer<>(
							(PortletRequest)servletRequest.getAttribute(
								JavaConstants.JAVAX_PORTLET_REQUEST),
							portletURL, null, null);

					List<SharedAssetsFilterItem> sharedAssetsFilterItems =
						_serviceTrackerList.toList();

					entriesSearchContainer.setResultsAndTotal(
						() -> sharedAssetsFilterItems,
						sharedAssetsFilterItems.size());

					return entriesSearchContainer;
				}

				@Override
				public boolean isShowBreadcrumb() {
					return false;
				}

				@Override
				public boolean isShowManagementToolbar() {
					return false;
				}

			});
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, SharedAssetsFilterItem.class,
			Collections.reverseOrder(
				new PropertyServiceReferenceComparator<>(
					"navigation.item.order")));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private static ServiceTrackerList<SharedAssetsFilterItem>
		_serviceTrackerList;
	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.singletonList(
			new SharedAssetsFilterItemItemSelectorReturnType());

	@Reference
	private ItemSelectorViewDescriptorRenderer
		<SharedAssetsFilterItemItemSelectorCriterion>
			_itemSelectorViewDescriptorRenderer;

	@Reference
	private Language _language;

}