/**
 * eGov suite of products aim to improve the internal efficiency,transparency, accountability and the service delivery of the
 * government organizations.
 * 
 * Copyright (C) <2015> eGovernments Foundation
 * 
 * The updated version of eGov suite of products as by eGovernments Foundation is available at http://www.egovernments.org
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/ or http://www.gnu.org/licenses/gpl.html .
 * 
 * In addition to the terms of the GPL license to be adhered to in using this program, the following additional terms are to be
 * complied with:
 * 
 * 1) All versions of this program, verbatim or modified must carry this Legal Notice.
 * 
 * 2) Any misrepresentation of the origin of the material is prohibited. It is required that all modified versions of this
 * material be marked in reasonable ways as different from the original version.
 * 
 * 3) This license does not grant any rights to any user of the program with regards to rights under trademark law for use of the
 * trade names or trademarks of eGovernments Foundation.
 * 
 * In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.android.view.adapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.egov.android.R;
import org.egov.android.listener.IActionListener;
import org.egov.android.model.Complaint;
import org.ocpsoft.prettytime.PrettyTime;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ComplaintAdapter extends BaseAdapter implements OnClickListener {

    private Activity activity;
    private ArrayList<Complaint> listItem;
    private ViewHolder holder = null;
    private IActionListener iListener = null;
    private boolean isPagination = false;
    private boolean isUsername = false;
    private int apiLevel = 0;

    /**
     * This adapter is used to show the complaint list
     * 
     * @param activity
     * @param listItem
     *            => list of complaints
     * @param isPagination
     *            => pagination flag
     * @param isUsername
     *            => has user name
     * @param apiLevel
     *            => android os api level
     * @param iListener
     *            => action listener
     */
    public ComplaintAdapter(Activity activity, ArrayList<Complaint> listItem, boolean isPagination,
            boolean isUsername, int apiLevel, IActionListener iListener) {
        this.activity = activity;
        this.listItem = listItem;
        this.iListener = iListener;
        this.isPagination = isPagination;
        this.apiLevel = apiLevel;
        this.isUsername = isUsername;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (apiLevel > 13) {
                convertView = inflater.inflate(R.layout.complaint_list_item, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.complaint_lower_version_list_item, parent,
                        false);
            }
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.status = (TextView) convertView.findViewById(R.id.complaint_status);
            holder.loadMore = (TextView) convertView.findViewById(R.id.load_more);
            holder.loadMore.setOnClickListener(this);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Complaint data = listItem.get(position);
        if (isUsername) {
            holder.name.setText(data.getCreatedBy());
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD);
        } else {
            holder.name.setVisibility(View.GONE);
        }
        File file = new File(data.getImagePath());

        if (file.exists()) {
            holder.image.setImageBitmap(_getBitmapImage(data.getImagePath()));
        } else {
            holder.image.setImageResource(R.drawable.complaint);
        }
        holder.description.setText(data.getDetails());
        holder.date.setText((data.getCreatedDate().equals("")) ? "" : getActualTime(data
                .getCreatedDate()));
        holder.status.setText(data.getStatus().toLowerCase());
        holder.loadMore.setTypeface(holder.loadMore.getTypeface(), Typeface.BOLD);

        if (getCount() > 5 && (getCount() % 5 == 1) && (position == getCount() - 1) && isPagination) {
            holder.loadMore.setVisibility(View.VISIBLE);
            ((ViewGroup) holder.loadMore.getParent()).getChildAt(0).setVisibility(View.GONE);
        } else {
            holder.loadMore.setVisibility(View.GONE);
            ((ViewGroup) holder.loadMore.getParent()).getChildAt(0).setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    /**
     * Function used to decode the file(for memory consumption) and return the bitmap to show it in
     * image view
     * 
     * @param path
     *            => image file path
     * @return bitmap
     */
    private Bitmap _getBitmapImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * Function used to format the date
     * 
     * @param datetime
     * @return
     */
    private String formatdate(String datetime) {
        String s = datetime;
        String[] parts = s.split("\\.");
        String part1 = parts[0];
        String s1 = part1.replace('T', '\t');
        String[] parts1 = s1.split("\\t");
        String date = parts1[0];
        String time = parts1[1];
        String newdate = date + " " + time;
        return newdate;
    }

    /**
     * Function used to get time difference between two dates
     * 
     * @param createdAt
     * @return
     */
    private String getActualTime(String createdAt) {
        Date date = new Date();
        String dateStart = formatdate(createdAt);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String current_date = format.format(date);

        Date d1 = null;
        Date d2 = null;

        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(current_date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000);
        long min = (diffHours * 60) + diffMinutes + (diffSeconds / 60);
        PrettyTime p = new PrettyTime();
        String currentTime = p.format(new Date(System.currentTimeMillis() - 1000 * 60 * min));
        if (currentTime.equalsIgnoreCase("moments from now")) {
            return currentTime.replaceAll(currentTime, "just now");
        }
        if (currentTime.equalsIgnoreCase("moments ago")) {
            return currentTime.replaceAll(currentTime, "1 minute ago");
        }
        return currentTime;
    }

    static class ViewHolder {
        TextView name;
        TextView description;
        TextView date;
        TextView loadMore;
        TextView status;
        ImageView image;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void onClick(View v) {
        if (iListener != null) {
            iListener.actionPerformed("LOAD_MORE", 0);
        }
    }
}
