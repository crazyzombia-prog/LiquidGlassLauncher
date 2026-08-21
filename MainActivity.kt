package com.example.liquidlauncher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class AppItem(val label:String,val pkg:String)

class MainActivity:ComponentActivity(){
    private fun loadApps():List<AppItem>{
        val pm=packageManager
        return pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),PackageManager.MATCH_ALL)
            .map{AppItem(it.loadLabel(pm).toString(),it.activityInfo.packageName)}
            .distinctBy{it.pkg}.sortedBy{it.label.lowercase()}
    }
    override fun onCreate(b:Bundle?){super.onCreate(b);setContent{LiquidLauncher(loadApps())}}

    @Composable fun LiquidLauncher(apps:List<AppItem>){
        var page by remember{mutableIntStateOf(0)}
        var library by remember{mutableStateOf(false)}
        var search by remember{mutableStateOf(false)}
        var folder by remember{mutableStateOf(false)}
        var q by remember{mutableStateOf("")}
        val pages=remember(apps){apps.chunked(20)}
        val filtered=apps.filter{it.label.contains(q,true)}
        Box(Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xff8ec5ff),Color(0xffe8c7ff),Color(0xffb7f4e5)))
        ).pointerInput(Unit){
            detectDragGestures{change,amount->
                change.consume()
                if(amount.x < -80) page=(page+1).coerceAtMost((pages.size-1).coerceAtLeast(0))
                if(amount.x > 80) page=(page-1).coerceAtLeast(0)
            }
        }){
            Column(Modifier.fillMaxSize().padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Spacer(Modifier.height(28.dp))
                Text(SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date()),fontSize=56.sp,color=Color.White)
                Text(SimpleDateFormat("EEEE, dd MMMM",Locale.getDefault()).format(Date()),color=Color.White.copy(.9f))
                Spacer(Modifier.height(18.dp))
                AnimatedContent(library,targetState=library,label="library"){isLib->
                    if(isLib) Glass {
                        Column{
                            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                                Text("Thư viện ứng dụng",fontSize=22.sp,color=Color.White)
                                Spacer(Modifier.weight(1f))
                                TextButton({library=false}){Text("Home")}
                            }
                            if(search) OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),
                                placeholder={Text("Tìm kiếm app")},singleLine=true)
                            LazyVerticalGrid(GridCells.Fixed(4),contentPadding=PaddingValues(6.dp)){
                                items(filtered){AppIcon(it)}
                            }
                        }
                    } else Glass {
                        Column{
                            LazyVerticalGrid(GridCells.Fixed(4),modifier=Modifier.heightIn(max=390.dp),contentPadding=PaddingValues(4.dp)){
                                items(pages.getOrNull(page).orEmpty()){AppIcon(it)}
                            }
                            Text("• ".repeat((pages.size.coerceAtLeast(1))),color=Color.White)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Glass{
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
                        pages.getOrNull(page)?.takeLast(4)?.forEach{AppIcon(it)}
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Button({search=!search},shape=RoundedCornerShape(24.dp)){Text("⌕")}
                    Button({library=!library},shape=RoundedCornerShape(24.dp)){Text(if(library)"Home" else "App Library")}
                    Button({folder=!folder},shape=RoundedCornerShape(24.dp)){Text("Folder")}
                }
                if(folder) Folder(apps.take(9)){folder=false}
            }
        }
    }

    @Composable fun Glass(content:@Composable()->Unit){
        Surface(Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
            color=Color.White.copy(.24f),tonalElevation=12.dp){Box(Modifier.padding(12.dp)){content()}}
    }
    @Composable fun AppIcon(a:AppItem){
        Column(Modifier.padding(6.dp).clickable{
            packageManager.getLaunchIntentForPackage(a.pkg)?.let{startActivity(it)}
        },horizontalAlignment=Alignment.CenterHorizontally){
            Surface(Modifier.size(60.dp),RoundedCornerShape(18.dp),color=Color.White.copy(.62f)){
                Box(contentAlignment=Alignment.Center){Text(a.label.take(1).uppercase(),fontSize=24.sp)}
            }
            Text(a.label.take(11),fontSize=10.sp,color=Color.White,maxLines=1)
        }
    }
    @Composable fun Folder(items:List<AppItem>,close:()->Unit){
        Box(Modifier.fillMaxSize().background(Color.Black.copy(.25f)),contentAlignment=Alignment.Center){
            Surface(Modifier.fillMaxWidth(.9f),shape=RoundedCornerShape(32.dp),color=Color.White.copy(.28f)){
                Column(Modifier.padding(16.dp)){
                    Row{Text("Ứng dụng",fontSize=22.sp,color=Color.White);Spacer(Modifier.weight(1f));TextButton(close){Text("Đóng")}}
                    LazyVerticalGrid(GridCells.Fixed(3)){items(items){AppIcon(it)}}
                }
            }
        }
    }
}
