package com.example.a503_14.a1024androidparsing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.os.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XMLParsing extends AppCompatActivity {

    //ListView에 출력될 데이터-M
    ArrayList<String> list;
    //출력을 위한 ListView-V
    ListView listView;
    //데이터와 ListView를 연결시켜줄 Adapter-C
    ArrayAdapter<String> adapter;

    //진행상황을 출력할 대화상자
    ProgressDialog progressDialog;
    //웹에서 다운로드 받을 스레드
    class ThreadEx extends Thread{
        @Override
        public void run() {
            //다운로드 받은 문자열을 저장할 객체
            StringBuilder sb = new StringBuilder();
            try {

                //문자열을 다운로드 받는 코드 영역
                URL url = new URL("http://www.hani.co.kr/rss/science/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setUseCaches(false);
                con.setConnectTimeout(20000);

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line + "\n");
                }
                br.close();
                con.disconnect();
                //Log.e("다운로드 받은 문자열", sb.toString());

            } catch (Exception e) {
                Log.e("다운로드 실패", e.getMessage());
            }


            //XML 파싱
            try {
                //파싱을 수행할 객체 생성
                DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
                DocumentBuilder builder=factory.newDocumentBuilder();

                //다운로드 받은 문자열을 InputStream으로 변환
                InputStream istream=new ByteArrayInputStream(sb.toString().getBytes("utf-8"));

                //메모리에 펼치기
                Document doc=builder.parse(istream);
                //루트 가져오기
                Element root=doc.getDocumentElement();

                //원하는 태그의 데이터를 가져오기
                NodeList items=root.getElementsByTagName("title");
                Log.e("items", items.toString());

                //반복문으로 태그를 순회(첫 번째는 가져오지 않기 위해 i초기값은 1)
                for(int i=1;i<items.getLength();i++){
                    //태그를 하나씩 가져오기
                    Node node=items.item(i);
                    //태그 안의 문자열을 가져와서 리스트에 추가
                    Node contents=node.getFirstChild();
                    String title=contents.getNodeValue();
                    list.add(title);
                }

                //핸들러를 호출(데이터 줄게 없으니까 sendEmptyMessage)
                handler.sendEmptyMessage(0);


            } catch (Exception e) {
                Log.e("XML파싱 실패", e.getMessage());
            }

        }
    }
    //화면을 갱신할 핸들러
    Handler handler =new Handler(){
      @Override
      public void handleMessage(Message message){
          progressDialog.dismiss();
          adapter.notifyDataSetChanged();
      }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmlparsing);

        //ListView를 출력하기 위한 데이터 생성
        list=new ArrayList<>();
        adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView=(ListView)findViewById(R.id.listview);
        //데이터와 ListView 연결
        listView.setAdapter(adapter);

        progressDialog = ProgressDialog.show(this, "한겨레", "다운로드 중");

        //스레드를 생성하고 시작
        ThreadEx th= new ThreadEx();
        th.start();


    }
}
