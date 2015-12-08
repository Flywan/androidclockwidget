/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.deskclock.clockwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Locale;

public class DigitalAppWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "DigitalAppWidgetProvider";

	/**
	 * Intent to be used for checking if a world clock's date has changed. Must
	 * be every fifteen minutes because not all time zones are hour-locked.
	 **/
	public static final String ACTION_ON_QUARTER_HOUR = "com.android.deskclock.ON_QUARTER_HOUR";

	// Lazily creating this intent to use with the AlarmManager
	private PendingIntent mPendingIntent;
	// Lazily creating this name to use with the AppWidgetManager
	private ComponentName mComponentName;

	public DigitalAppWidgetProvider() {
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		super.onReceive(context, intent);

		if (ACTION_ON_QUARTER_HOUR.equals(action)
				|| Intent.ACTION_DATE_CHANGED.equals(action)
				|| Intent.ACTION_TIMEZONE_CHANGED.equals(action)
				|| Intent.ACTION_TIME_CHANGED.equals(action)
				|| Intent.ACTION_LOCALE_CHANGED.equals(action)) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			if (appWidgetManager != null) {
				int[] appWidgetIds = appWidgetManager
						.getAppWidgetIds(getComponentName(context));
				for (int appWidgetId : appWidgetIds) {
					RemoteViews widget = new RemoteViews(
							context.getPackageName(),
							R.layout.digital_appwidget);
					float ratio = WidgetUtils.getScaleRatio(context, null,
							appWidgetId);
					WidgetUtils.setTimeFormat(
							widget,
							(int) context.getResources().getDimension(
									R.dimen.widget_label_font_size),
							R.id.the_clock);
					WidgetUtils.setClockSize(context, widget, ratio);
					appWidgetManager.partiallyUpdateAppWidget(appWidgetId,
							widget);
				}
			}
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			float ratio = WidgetUtils.getScaleRatio(context, null, appWidgetId);
			updateClock(context, appWidgetManager, appWidgetId, ratio);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		// scale the fonts of the clock to fit inside the new size
		float ratio = WidgetUtils.getScaleRatio(context, newOptions,
				appWidgetId);
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		updateClock(context, widgetManager, appWidgetId, ratio);
	}

	private void updateClock(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId, float ratio) {
		RemoteViews widget = new RemoteViews(context.getPackageName(),
				R.layout.digital_appwidget);
		// Launch clock when clicking on the time in the widget only if not a
		// lock screen widget
		Bundle newOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
		if (newOptions != null
				&& newOptions.getInt(
						AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1) != AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) {
			Intent clockIntent = new Intent();
			clockIntent.setComponent(new ComponentName("com.android.deskclock",
					"com.android.deskclock.DeskClock"));
			widget.setOnClickPendingIntent(R.id.digital_appwidget,
					PendingIntent.getActivity(context, 0, clockIntent, 0));
		}

		WidgetUtils.setTimeFormat(widget, (int) context.getResources()
				.getDimension(R.dimen.widget_label_font_size), R.id.the_clock);
		WidgetUtils.setClockSize(context, widget, ratio);

		// Set today's date format
		CharSequence dateFormat = DateFormat.getBestDateTimePattern(
				Locale.getDefault(),
				context.getString(R.string.abbrev_wday_month_day_no_year));

		widget.setCharSequence(R.id.date, "setFormat12Hour", dateFormat);
		widget.setCharSequence(R.id.date, "setFormat24Hour", dateFormat);
		Log.d(TAG, "dateFormat" + dateFormat);

		// Set up the click on any world clock to start the Cities Activity
		// TODO: Should this be in the options guard above?
		/*
		 * widget.setPendingIntentTemplate(R.id.digital_appwidget_listview,
		 * PendingIntent. getActivity(context, 0, new Intent(context,
		 * CitiesActivity.class), 0));
		 */
		appWidgetManager.updateAppWidget(appWidgetId, widget);
	}

	/**
	 * Create the component name for this class
	 * 
	 * @param context
	 *            The Context in which the widgets for this component are
	 *            created
	 * @return the ComponentName unique to DigitalAppWidgetProvider
	 */
	private ComponentName getComponentName(Context context) {
		if (mComponentName == null) {
			mComponentName = new ComponentName(context, getClass());
		}
		return mComponentName;
	}
}
