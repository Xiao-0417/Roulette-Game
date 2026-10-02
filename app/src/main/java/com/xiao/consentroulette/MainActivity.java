package com.xiao.consentroulette;

import android.app.*; import android.os.*; import android.content.*; import android.graphics.Color; import android.view.*; import android.widget.*;
import org.json.*; import java.time.*; import java.time.temporal.ChronoUnit; import java.util.*;

public class MainActivity extends Activity {
  private static final String STORE="wheel_state"; private final String[] names={"专注任务","运动计划","学习目标","睡眠习惯","社交练习","放松时光"};
  private JSONObject state; private LinearLayout page; private final Random random=new Random();
  @Override public void onCreate(Bundle b){super.onCreate(b); load(); showHome();}
  void load(){ try{state=new JSONObject(getPreferences(0).getString(STORE,"{}"));}catch(Exception e){state=new JSONObject();} }
  void save(){getPreferences(0).edit().putString(STORE,state.toString()).apply();}
  TextView text(String value,int size){ TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.rgb(65,24,49));t.setPadding(24,16,24,16);return t; }
  Button button(String label){ Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundColor(Color.rgb(122,21,80));b.setPadding(20,12,20,12); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(24,8,24,8);b.setLayoutParams(p);return b; }
  void base(){ ScrollView s=new ScrollView(this);page=new LinearLayout(this);page.setPadding(12,20,12,32);page.setOrientation(LinearLayout.VERTICAL);page.setBackgroundColor(Color.rgb(255,247,251));s.addView(page);setContentView(s); }
  void showHome(){ base(); page.addView(text("同意轮盘",28)); page.addView(text("成年人自愿挑战追踪器 · 本地保存 · 可随时暂停",15));
    if(!state.has("start")){ page.addView(text("开始前请确认：所有挑战必须由成年人自愿设定；可随时暂停或结束。",16)); Button start=button("创建挑战");start.setOnClickListener(v->showSetup());page.addView(start); return; }
    LocalDate start=LocalDate.parse(state.optString("start")); long day=ChronoUnit.DAYS.between(start,LocalDate.now())+1; boolean endless=state.optBoolean("endless"); int duration=state.optInt("duration",14);
    page.addView(text(endless?"无尽模式 · 第 "+day+" 天":"进度：第 "+Math.max(1,day)+" / "+duration+" 天",20));
    if(!endless && day>duration){page.addView(text("本轮挑战已完成。所有记录仍保存在本机。",18)); Button reset=button("开始新的挑战");reset.setOnClickListener(v->{state=new JSONObject();save();showHome();});page.addView(reset);return;}
    page.addView(text("今天："+LocalDate.now()+"\n每日仅可记录一次摇骰；分数 ≤ −5 的类别禁用，≥ 10 的类别锁定。",15));
    for(int i=0;i<names.length;i++) page.addView(text(names[i]+"："+state.optInt("s"+i,0)+scoreNote(state.optInt("s"+i,0)),17));
    if(RuleEngine.canPlayToday(state.optString("last",null),LocalDate.now())){Button play=button("开始今日轮盘");play.setOnClickListener(v->showDraw());page.addView(play);}else page.addView(text("今日已完成记录。请在明天回来。",17));
    Button pause=button("暂停 / 结束挑战");pause.setOnClickListener(v->confirmEnd());page.addView(pause);
  }
  String scoreNote(int s){return s<=-5?"（已禁用）":s>=10?"（已锁定）":"";}
  void showSetup(){base();page.addView(text("创建挑战",26));page.addView(text("周期：14–28 天，或无尽模式。任务类别为安全、非露骨的占位项目，你可以在日常生活中自愿定义具体内容。",15));
    Spinner duration=new Spinner(this);String[] options=new String[16];for(int i=0;i<15;i++)options[i]=(14+i)+" 天";options[15]="无尽模式";duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,options));page.addView(duration);
    Button create=button("确认并开始");create.setOnClickListener(v->{try{state.put("start",LocalDate.now().toString());state.put("endless",duration.getSelectedItemPosition()==15);state.put("duration",14+Math.min(14,duration.getSelectedItemPosition()));for(int i=0;i<6;i++)state.put("s"+i,0);save();showHome();}catch(Exception ignored){}});page.addView(create);
  }
  void showDraw(){ base();page.addView(text("今日轮盘",26));page.addView(text("选择一个 V 类别，系统将随机选择不同的 W 类别；随后分别摇 X 与 Y 骰子。",16)); Spinner v=new Spinner(this);v.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));page.addView(v);Button roll=button("摇骰子");roll.setOnClickListener(x->{int vi=v.getSelectedItemPosition(),wi;do{wi=random.nextInt(names.length);}while(wi==vi); int xv=RuleEngine.roll(random),yv=RuleEngine.roll(random);applyFlow(vi,wi,xv,yv);});page.addView(roll); }
  void applyFlow(int vi,int wi,int xv,int yv){ int vs=state.optInt("s"+vi),ws=state.optInt("s"+wi); if(vs<=-5||ws<=-5||vs>=10||ws>=10){new AlertDialog.Builder(this).setMessage("所选类别或随机类别已禁用/锁定；请重新选择。 ").setPositiveButton("返回",(d,w)->showDraw()).show();return;} ChoiceDialog(vi,wi,xv,yv); }
  void ChoiceDialog(int vi,int wi,int xv,int yv){ String hint="V（"+names[vi]+"）摇到 X"+xv+"；W（"+names[wi]+"）摇到 Y"+yv+"。"; boolean choice=xv==1||yv==1||yv==6; if(!choice){finishDay(vi,wi,xv,yv,null,null);return;} new AlertDialog.Builder(this).setTitle("选择结果").setMessage(hint+"\n当骰子为 1 或 Y=6 时需要选择规则分支。").setSingleChoiceItems(new String[]{"默认：−2 / Y+4","取整减半（骰子 1）/ Y 翻倍（Y=6）"},0,null).setPositiveButton("应用",(d,w)->{int pick=((AlertDialog)d).getListView().getCheckedItemPosition(); finishDay(vi,wi,xv,yv,pick==1?RuleEngine.Choice.HALF_UP:RuleEngine.Choice.MINUS_TWO,pick==1?RuleEngine.Choice.DOUBLE:null);}).show(); }
  void finishDay(int vi,int wi,int xv,int yv,RuleEngine.Choice xc,RuleEngine.Choice yc){ RuleEngine.Choice yChoice = yv == 1 ? (yc == RuleEngine.Choice.DOUBLE ? RuleEngine.Choice.HALF_UP : RuleEngine.Choice.MINUS_TWO) : yc; RuleEngine.Result a=RuleEngine.apply(state.optInt("s"+vi),RuleEngine.Die.X,xv,xc);RuleEngine.Result b=RuleEngine.apply(state.optInt("s"+wi),RuleEngine.Die.Y,yv,yChoice);try{state.put("s"+vi,a.after);state.put("s"+wi,b.after);state.put("last",LocalDate.now().toString());save();}catch(Exception ignored){} new AlertDialog.Builder(this).setTitle("今日结果").setMessage(names[vi]+"："+a.before+" → "+a.after+"\n"+names[wi]+"："+b.before+" → "+b.after+"\n\n记录已保存到本机。").setPositiveButton("完成",(d,w)->showHome()).show(); }
  void confirmEnd(){new AlertDialog.Builder(this).setTitle("结束挑战？").setMessage("结束后将清除本机的本轮记录。你也可以直接关闭应用以暂停。 ").setNegativeButton("取消",null).setPositiveButton("结束并清除",(d,w)->{state=new JSONObject();save();showHome();}).show();}
}
