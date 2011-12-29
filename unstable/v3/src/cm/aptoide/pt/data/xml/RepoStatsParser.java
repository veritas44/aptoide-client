/**
 * RepoStatsParser, 	auxiliary class to Aptoide's ServiceData
 * Copyright (C) 2011 Duarte Silveira
 * duarte.silveira@caixamagica.pt
 * 
 * derivative work of previous Aptoide's RssHandler with
 * Copyright (C) 2009  Roberto Jacinto
 * roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package cm.aptoide.pt.data.xml;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import cm.aptoide.pt.data.Constants;
import cm.aptoide.pt.data.model.ViewStatsInfo;

/**
 * RepoStatsParser, handles Stats Repo xml Sax parsing
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class RepoStatsParser extends DefaultHandler{
	private ManagerXml managerXml = null;
	
	private ViewXmlParse parseInfo;
	private ViewStatsInfo stats;	
	private ArrayList<ViewStatsInfo> statsList = new ArrayList<ViewStatsInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
	private ArrayList<ArrayList<ViewStatsInfo>> statsListInsertStack = new ArrayList<ArrayList<ViewStatsInfo>>(2);
	
	private EnumXmlTagsStats tag = EnumXmlTagsStats.apklst;
	private HashMap<String, EnumXmlTagsStats> tagMap = new HashMap<String, EnumXmlTagsStats>();
	

	private int appHashid = Constants.EMPTY_INT;
	private int appFullHashid = Constants.EMPTY_INT;
	private int likes = Constants.EMPTY_INT;
	private int parsedAppsNumber = Constants.EMPTY_INT;
	
	private StringBuilder tagContentBuilder;
	
		
	public RepoStatsParser(ManagerXml managerXml, ViewXmlParse parseInfo){
		this.managerXml = managerXml;
		this.parseInfo = parseInfo;
		
		for (EnumXmlTagsStats tag : EnumXmlTagsStats.values()) {
			tagMap.put(tag.name(), tag);
		}
	}
	
	public RepoStatsParser(ManagerXml managerXml, ViewXmlParse parseInfo, int appHashid){
		this(managerXml, parseInfo);
		this.appHashid = appHashid;
	}
	
	@Override
	public void characters(final char[] chars, final int start, final int length) throws SAXException {
		super.characters(chars, start, length);
		
		tagContentBuilder.append(new String(chars, start, length).trim());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		switch (tag) {
			case apphashid:
				appFullHashid = (Integer.parseInt(tagContentBuilder.toString())+"|"+parseInfo.getRepository().getHashid()).hashCode();
				stats = new ViewStatsInfo(appFullHashid);
				break;
				
			case dwn:
				stats.setDownloads(Integer.parseInt(tagContentBuilder.toString()));
				break;
				
			case likes:
				likes = Integer.parseInt(tagContentBuilder.toString());
				break;
				
			case dislikes:
				stats.setLikesDislikes(likes, Integer.parseInt(tagContentBuilder.toString()));
				break;
				
			default:
				break;
		}
		
		if(localName.trim().equals(EnumXmlTagsStats.pkg.toString())){
			if(parsedAppsNumber >= Constants.APPLICATIONS_IN_EACH_INSERT){
				parsedAppsNumber = 0;
				statsListInsertStack.add(statsList);

				Log.d("Aptoide-RepoStatsParser", "bucket full, inserting stats: "+statsList.size());
				try{
					new Thread(){
						public void run(){
							this.setPriority(Thread.MAX_PRIORITY);
							final ArrayList<ViewStatsInfo> statsInserting = statsListInsertStack.remove(Constants.FIRST_ELEMENT);
							
							managerXml.getManagerDatabase().insertStats(statsInserting);
						}
					}.start();
	
				} catch(Exception e){
					/** this should never happen */
					//TODO handle exception
					e.printStackTrace();
				}
				
				statsList = new ArrayList<ViewStatsInfo>(Constants.APPLICATIONS_IN_EACH_INSERT);
			}
			parsedAppsNumber++;
			parseInfo.getNotification().incrementProgress(1);
			
			statsList.add(stats);
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);

		tagContentBuilder = new StringBuilder();
		tag = tagMap.get(localName.trim());
	}
	
	
	
	
	@Override
	public void startDocument() throws SAXException {	//TODO refacto Logs
		Log.d("Aptoide-RepoStatsHandler","Started parsing XML from " + parseInfo.getRepository() + " ...");
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		Log.d("Aptoide-RepoStatsHandler","Done parsing XML from " + parseInfo.getRepository() + " ...");
		
		if(!statsList.isEmpty()){
			Log.d("Aptoide-RepoStatsParser", "bucket not empty, stats: "+statsList.size());
			statsListInsertStack.add(statsList);
		}

		Log.d("Aptoide-RepoInfoParser", "buckets: "+statsListInsertStack.size());
		while(!statsListInsertStack.isEmpty()){
			managerXml.getManagerDatabase().insertStats(statsListInsertStack.remove(Constants.FIRST_ELEMENT));			
		}
		
		if(appHashid != Constants.EMPTY_INT){
			managerXml.parsingRepoAppStatsFinished(parseInfo.getRepository(), appHashid);
		}else{
			managerXml.parsingRepoStatsFinished(parseInfo.getRepository());			
		}
		super.endDocument();
	}


}