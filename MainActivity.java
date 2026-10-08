package com.quickgallery.app;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
 final ArrayList<Item> items=new ArrayList<>(); final Set<String> excluded=new HashSet<>();
 LinearLayout root; String folder=null; int current=0; Handler handler=new Handler();
 static class Item { Uri uri; String path; boolean video; Item(Uri u,String p,boolean v){uri=u;path=p;video=v;} }
 @Override public void onCreate(Bundle b){super.onCreate(b); excluded.addAll(getPreferences(0).getStringSet("excluded",new HashSet<>())); if(android.os.Build.VERSION.SDK_INT>=33)requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED},1); else requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1); scan();}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);scan();}
 void shell(String title){root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(Color.rgb(22,22,22));setContentView(root);TextView bar=new TextView(this);bar.setText(title);bar.setTextSize(21);bar.setTextColor(-1);bar.setPadding(20,22,12,22);root.addView(bar);}
 Button button(String s,LinearLayout l,Runnable r){Button b=new Button(this);b.setText(s);l.addView(b);b.setOnClickListener(v->r.run());return b;}
 void scan(){items.clear(); load(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,false);load(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,true);showFolders();}
 void load(Uri base,boolean video){String[] cols={MediaStore.MediaColumns._ID,MediaStore.MediaColumns.RELATIVE_PATH};try(Cursor c=getContentResolver().query(base,cols,null,null,MediaStore.MediaColumns.DATE_MODIFIED+" DESC")){if(c!=null)while(c.moveToNext()){long id=c.getLong(0);String p=c.getString(1);if(p==null)p="Unknown/";items.add(new Item(android.content.ContentUris.withAppendedId(base,id),p,video));}}catch(Exception ignored){} }
 boolean hidden(String path){for(String x:excluded)if(path.startsWith(x))return true;return false;}
 void showFolders(){folder=null;shell("QuickGallery · Folders");button("Manage excluded folders",root,()->manage());ScrollView scroll=new ScrollView(this);root.addView(scroll);LinearLayout list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);TreeMap<String,Integer> counts=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);for(Item i:items)if(!hidden(i.path))counts.put(i.path,counts.getOrDefault(i.path,0)+1);for(String name:counts.keySet())button("📁  "+name+" ("+counts.get(name)+")",list,()->showAlbum(name));if(counts.isEmpty()){TextView t=new TextView(this);t.setText("No visible media. Grant photo/video permission in Android Settings.");t.setTextColor(-1);list.addView(t);}}
 void manage(){shell("Excluded folders");button("← Back",root,()->showFolders());TreeSet<String> folders=new TreeSet<>();for(Item i:items)folders.add(i.path);ScrollView sc=new ScrollView(this);root.addView(sc);LinearLayout list=new LinearLayout(this);list.setOrientation(1);sc.addView(list);for(String p:folders){CheckBox cb=new CheckBox(this);cb.setText(p);cb.setTextColor(-1);cb.setChecked(excluded.contains(p));list.addView(cb);cb.setOnCheckedChangeListener((v,on)->{if(on)excluded.add(p);else excluded.remove(p);getPreferences(0).edit().putStringSet("excluded",new HashSet<>(excluded)).apply();});}}
 void showAlbum(String name){folder=name;shell(name);button("← Folders",root,()->showFolders());ScrollView sc=new ScrollView(this);root.addView(sc);GridLayout grid=new GridLayout(this);grid.setColumnCount(3);sc.addView(grid);for(int n=0;n<items.size();n++){Item item=items.get(n);if(!name.equals(item.path))continue;final int index=n;ImageView iv=new ImageView(this);int w=getResources().getDisplayMetrics().widthPixels/3;GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=w;lp.height=w;lp.setMargins(2,2,2,2);iv.setLayoutParams(lp);iv.setScaleType(ImageView.ScaleType.CENTER_CROP);grid.addView(iv);try{iv.setImageBitmap(getContentResolver().loadThumbnail(item.uri,new android.util.Size(240,240),null));}catch(Exception ignored){}iv.setOnClickListener(v->view(index));}}
 void view(int index){current=index;Item item=items.get(index);if(item.video){play(item);return;}shell("Photo");button("← Album",root,()->showAlbum(item.path));ImageView photo=new ImageView(this);photo.setBackgroundColor(Color.BLACK);photo.setScaleType(ImageView.ScaleType.FIT_CENTER);root.addView(photo,new LinearLayout.LayoutParams(-1,0,1));try{photo.setImageURI(item.uri);}catch(Exception ignored){}final float[] start={0};photo.setOnTouchListener((v,e)->{if(e.getAction()==0){start[0]=e.getX();return true;}if(e.getAction()==1){float dx=e.getX()-start[0];if(Math.abs(dx)>70){int dir=dx<0?1:-1;for(int n=index+dir;n>=0&&n<items.size();n+=dir)if(!items.get(n).video&&items.get(n).path.equals(item.path)){view(n);break;}}return true;}return true;});}
 void play(Item item){shell("Video");button("← Album",root,()->showAlbum(item.path));VideoView vv=new VideoView(this);vv.setBackgroundColor(Color.BLACK);root.addView(vv,new LinearLayout.LayoutParams(-1,0,1));vv.setVideoURI(item.uri);vv.setOnPreparedListener(mp->vv.start());TextView seek=new TextView(this);seek.setTextColor(-1);seek.setPadding(16,12,16,12);seek.setText("Swipe horizontally on video to scrub");root.addView(seek);final float[] x={0};final int[] base={0};vv.setOnTouchListener((v,e)->{if(e.getAction()==0){x[0]=e.getX();base[0]=vv.getCurrentPosition();return true;}if(e.getAction()==1||e.getAction()==2){int duration=vv.getDuration();if(duration>0){int target=Math.max(0,Math.min(duration,base[0]+(int)((e.getX()-x[0])/Math.max(1,v.getWidth())*duration)));vv.seekTo(target);seek.setText(String.format(java.util.Locale.US,"%02d:%02d / %02d:%02d",target/60000,target/1000%60,duration/60000,duration/1000%60));}return true;}return false;});}
 @Override public void onBackPressed(){if(folder!=null)showAlbum(folder);else super.onBackPressed();}
}
