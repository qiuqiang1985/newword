package org.ansj.app.newWord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import love.cq.util.StringUtil;

import org.ansj.app.newWord.PatHashMap.Node;
import org.ansj.dic.DicReader;
import org.ansj.domain.NewWord;
import org.ansj.domain.Term;
import org.ansj.domain.TermNatures;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.Graph;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.tensyn.TensynRecordReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 糊弄写的新词发现
 * 
 * @author ansj
 * 
 */
public class NewWordDetection {

	/**
	 * 分词结果的新词发现
	 * 
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public Collection<Node> getNewWords(Graph graph) throws IOException {
		// 构建pat-tree
		PatHashMap pt = makePatHash(graph);
		// 从pat-tree中查找出最大公共字串
		Collection<Node> words = pt.getWords();
		return words;
	}

	/**
	 * 构建pat-tree
	 * 
	 * @param terms
	 * @return
	 */
	private PatHashMap makePatHash(Graph graph) {
		PatHashMap pt = new PatHashMap();
		// 一次遍历增加到树中,ps:树内部进行了n^2次遍历,填充真个pat结构
		List<Term> tempList = new ArrayList<Term>();
		for (Term term : graph.terms) {
			if (term == null) {
				continue;
			}
			if (filter(term)) {
				tempList.add(term);
			} else {
				// 如果大于,则放到树中
				if (tempList.size() > 1) {
					// 计算分数.并且增加到pattree中
					pt.addList(tempList);
				}
				if (tempList.size() > 0) {
					if (tempList.size() < 10) {
						tempList.clear();
					} else {
						tempList = new ArrayList<Term>();
					}
				}
			}
		}
		return pt;
	}

	/**
	 * 监督.来确定此词是否.可能是新词的一部分
	 * 
	 * @param term
	 * @return
	 */
	private boolean filter(Term term) {
		int length = term.getName().length()  ;
		//只对单字新词发现
		if(length>1){
			return false ;
		}
		// 用停用词过滤.
		if (hs.contains(term.getName()) || term.getName().trim().length() == 0) {
			return false;
		}

		// 词性过滤
		String natureStr = term.getNatrue().natureStr;
		if (natureStr.contains("m") || ("v".equals(natureStr) && term.getTermNatures().allFreq > 100*length)
				|| (("d".equals(natureStr)) && term.getTermNatures().allFreq > 1000) || "z".equals(natureStr) || term.getTermNatures() == TermNatures.NB
				//|| term.getTermNatures() == TermNatures.EN
				) {
			return false;
		}

		return true;
	}

	private static final HashSet<String> hs = new HashSet<String>();

	/**
	 * 加载停用词典
	 */
	static {
		
		BufferedReader filter = null;
		try {
			filter = DicReader.getReader("newWord/newWordFilter.dic");
			String temp = null;
			while ((temp = filter.readLine()) != null) {
				hs.add(temp.toLowerCase());
			}
			hs.add("－");
			hs.add("　");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (filter != null)
				try {
					filter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public static void main(String[] args) throws IOException {

//		String content = "e人是什么，E人E本可以说是评测中心的老朋友了，经历了三代升级，在年末的时候我们拿到了第四代产品——E人E本T4，熟悉E人E本的朋友都知道，这款平板电脑定位于商务应用，与市售大多数娱乐平板有着本质的区别，凭借原笔迹数字书写技术，E人E本获得了国内高端政商用户的认可，作为即将投放在2012年的新品，T4有着怎样的表现呢？";
//		 "Adobe在今年3月份称，从8月1日开始，如果开发者的应用使用了Flash Player的高级功能（即支持硬件加速的Stage 3D和域内存功能），且净收入超过了5万美元，则需要向Adobe公司支付净收入的9%。 Flash Player的高级功能主要针对使用C/C++（通过Alchemy编译器）或第三方工具（如Unity）开发Flash Player平台商业游戏的开发商。 近日 Adobe 称，将延长这一决策开始执行的时间，开发者至少还有8周的时间来准备以及获得许可证。 Adobe表示，将会提供一个网站，开发者可以在8月底之前通过该网站获取许可证。 Flash Player开始执行高级功能许可要求后，需要使用Flash Player高级功能而未被许可的应用程序，将自动使用软件渲染功能来运行。 ";
//		 content =
//		 "PhoneGap开发团队近日正式发布了PhoneGap 2.0版本。 PhoneGap是一个开源的跨平台移动应用开发框架，能使开发者们在只使用标准网络技术（HTML5、CSS和JavaScript）的情况下开发跨平台应用。PhoneGap的目标是实现“一次开发，运行于任何移动终端平台”。 PhoneGap最初由Nitobi开发，2011年10月，Nitobi被Adobe收购，而PhoneGap项目也被贡献给Apache软件基金会，并有了一个新的名字Apache Cordova。 PhoneGap 2.0 的新特性包括： Cordova WebView：允许将PhoneGap作为一个视图片段整合进更大的本地应用程序中。命令行工具（CLI）：可用于Android、iOS和BlackBerry平台，为跨平台任务（如创建、调试、模拟等）提供了一个标准的命令操作方式。文档改善：包括快速入门指南、插件迁移指南以及其他文档，以帮助开发者加快和简化移动应用的开发，Web Inspector Remote（Weinre）移植到nodejs：意味着可以通过NMP（Node Package Manager）来轻松安装。Cordovajs：性能、安全性、平台间API一致性得到了显著改进。项目过渡到Apache Cordova，即将从孵化器中毕业。改善了iOS应用的创建PhoneGap首席开发人员Brian•LeRoux表示： 引用PhoneGap 2.0大大提升了开发人员的体验。我们的文档现在更全面，其中包含了开发者所要求的Plugin API。开发者将可以使用PhoneGap作为桥梁，创建自己的浏览器API，以用于本地调用。";
//		 content ="2014-12-03，仓老师的另一种哲学，是她对生命生活的泰然和淡定，生命都是不能选择的也是不能比较的，正如每一双看着苍老师身体的那些眼睛一样，她无法选择哪一双眼睛能在她的身体上停留而哪一双不能，她也无法比较哪一双眼睛是欣赏而哪一双是亵渎，老师只是从容的对视着每一双看着她的眼睛，淡然的面对那每一声扑面而来的急促喘息，扭一扭身子，像大海一般，静静的容纳，静静的消化....。她说这是她的职业也是她的生活，能用敬业去诠释无法选择的生命，这是种有道德的职业，能勇敢的面对有道德的职业去坦然的生活，这是种彪悍的人生，而彪悍的人生从来就不需要解释!她用一种哲的姿态告诉我们，老天也许不公，富贵贫穷差异比比皆是，生命也许随时都会遇上坎坷和撞击，但，最后都会过去的，解开的扣子会再次扣上，喘息也总有一个时刻会安静下来，这种深刻也正是这个国度的最真实写照。当呼吸声渐渐平息，撞击声嘎然而止，短暂的寂静过后，又一个新朝代将撩开面纱!";
//		 "每每谈起苍井空老师，总有些爱国者跳出来声嘶力竭的声讨，原因有二，一，她不是中国人，二，她从事的是AV行业，其实这些声讨者都是忘本的种类，他们的祖宗不AV，他们的父母从哪里来?他们的父母不AV，他们又从哪里来?娶过了老婆骂媒人，用完了人民踹百姓这也正是这个朝代最亮眼的一道时代标志!说起爱国，人不该把简单的事情复杂化，为什么爱国?我们之所以爱国，那是因为我们只有这地方的身份证，只有这个到处都在拆的地方可以供我们容身苟活，木有别的地方可以让我们去!神马眼中饱含着滚烫的热泪....深深眷恋着这块土地....母亲，我深情的呼唤.....，这些空话大话，不是脑白金吃多了就是新闻联播看多了!说出来你自己相信么!?人可以不要脸，但爱国若爱到如此不要脸，就不厚道了!虽然现今不是爱国情绪高涨的战争年代，但苍井空老师在和平年代穿越口水东渡而来解救憋屈青年和白求恩医生在战争年代穿越炮火西行而来救死扶伤并没有什么区别，都是国际的人道主义精神，都是国际援助的白求恩精神!";
//
//";
//		 content =
//		 "苹果今天正式发布了新一代操作系统OS X Mountain Lion，版本号为10.8，该系统已经在Mac App Store上架，售价为128元。 苹果称，OS X Mountain Lion中包含了200多项新功能，并将iPhone、iPad和iPod touch中的诸多精彩功能带到了Mac上。此外，该系统还针对中国用户进行了本土化定制。 1.  Mountain Lion的中国本土化 OS X Mountain Lion 针对许多热门的中文功能与服务提供全新支持：词典 app 现包含《现代汉语规范词典》。通过升级的文本输入方法，输入中文变得更轻松、更快速、更准确。通过八种全新字体，你的书写内容可以用正式、非正式或有趣的形式来呈现。Mail 支持 QQ、163 和 126 邮箱。Safari中内置了百度搜索选项。现在，你还可以从你的 app 直接把内容发布到网上：在优酷和土豆上发布视频，在新浪微博上发布微博。2.  iCloud iCloud可以让Mac、iPad、iPhone 和iPod touch之间的协作更加紧密。无论你在哪里使用电子邮件、日历、通讯录、提醒事项、文档、备忘录等内容，它都会让它们保持更新。只需用你的 Apple ID 登录一次，iCloud 就可以在所有使用它的 app 中设置好了。 3.  iMessage 该系统还引入了iMessage功能。现在你也可以向任何运行 iOS 5 的 iPhone、iPad 或 iPod touch 用户发送信息。信息会在你的 Mac 及你使用的任何设备上显示，这就意味着你可以在 Mac 上开始对话，然后无论走到哪里都能在 iPhone 或 iPad 上继续进行。你还可以发送照片、视频、文档和通讯录，甚至发送群组消息。 4.  通知中心 苹果还将iOS 5中的通知中心引入到了Mountain Lion中，你可以在通知中心中轻松查看电子邮件、信息、软件更新或日历提醒等消息。如果有任何通知，你就会在第一时间知道。 5.  Gatekeeper Gatekeeper可以帮助你避免在 Mac 上下载和安装恶意软件，还能为你进一步控制哪些 app 可以被安装。这是 OS X 保卫 Mac 安全的一种全新方式。 此外，OS X Mountain Lion在日历、通讯录、信息、文本编辑、分享等方面进行了大量改进，详细信息可参阅：OS X Mountain Lion新特性 ";
//		 content =
//		 "甲骨文的MySQL开发者工具团队今天发布了MySQL Workbench 5.2.41版本，该版本中包含了一个新的数据库迁移向导插件。 新的迁移向导提供了一个易于使用的图形界面，帮助开发者将数据库从第三方产品中迁移到MySQL。在这个初期版本的插件中，支持迁移的数据库管理系统包括微软的SQL Server，以及其他支持ODBC的数据库，如PostgreSQL等。 MySQL Workbench是一个可视化的数据库设计软件，前身是 FabForce 公司的 DB Designer 4。它为数据库管理员、程序开发者和系统规划师提供可视化设计、模型建立、以及数据库管理功能。 除了迁移工具外，MySQL Workbench 5.2.41中的其他改进包括： 修复了100多个bugSQL编辑器中的代码完成功能（测试版）更好地处理建模时的模式同步";
//		 content ="分享屌丝泡妞心得";
				 //"芙蓉姐姐（1977-），陕西咸阳人，原名史恒侠（笔名林可），又名火冰可儿、清水芙蓉、水媚妖姬、黑桃皇后。陕西工学院机械系毕业，曾是考清华大学研究生大军中的一员，由于网络拍客将其照片上传到水木清华、北大未名和MOP网站上，成为了网络上人气火爆的红人，被称呼为芙蓉姐姐。2011年7月8日，芙蓉姐姐以一身青花瓷造型重回清华拍写真，尽展瘦身正果。2011年7月30日，芙蓉姐姐开演唱会，自称女儿国国宝。2013年6月暴瘦一圈的她穿着水红色紧身裙惊艳亮相某活动。尽管利物浦近5次联赛做客对阵斯托克城2平3负未曾取胜，但近32个主场对阵斯托克城取得27胜5平保持不败，斯托克城上次在安菲尔德客胜还是1959年的乙级联赛。双方历史交锋128场，利物浦64胜35平29负占据上风。利物浦近4个赛季首轮主场2平客场2负未尝胜绩。斯托克城近5个赛季首轮仅1胜。利物浦新援图雷、阿斯帕斯和米格诺列均首发出场，小将阿尔贝托进入替补席。苏亚雷斯停赛缺阵。分手神器和黄金手指是什么？邱强在江西都昌周溪。我不喜欢日本和服.我来到北京大学.他来到了网易杭研大厦,打酱油这个词现在非常火，这是怎么了？快打酱油把！，打酱油，打酱油，屌丝库告诉你什么是屌丝、屌丝是什么意思,这里是屌丝男、女屌丝的天地,屌丝库有屌丝生活、屌丝图片、男女屌丝日志、网络新词等栏目,分享屌丝泡妞心得,屌丝的生活点点滴滴.2013/14赛季西甲联赛仅开战一轮，梅西就已经又创造了一项队史新纪录，在对阵莱万特的比赛中梅开二度后，梅西的西甲联赛进球场次达到了133场，从而超过塞萨尔保持的132场队史纪录，属于金球先生的刷纪录模式在新赛季依旧疯狂地持续着，今夜后梅西的职业生涯正式比赛进球总数已达350粒，而比这更令人欣慰的是，马尔蒂诺敢在比赛第70分钟就将梅西换下，这可是3年以来的第一次。";
		// "分手神器和黄金手指是什么？邱强在江西都昌周溪。我不喜欢日本和服.我来到北京大学.他来到了网易杭研大厦,打酱油这个词现在非常火，这是怎么了？快打酱油把！，打酱油，打酱油，屌丝库告诉你什么是屌丝、屌丝是什么意思,这里是屌丝男、女屌丝的天地,屌丝库有屌丝生活、屌丝图片、男女屌丝日志、网络新词等栏目,分享屌丝泡妞心得,屌丝的生活点点滴滴";
		//
//		 content = "江苏宏宝五金股份有限公司（以下简称“本公司”）于2012年11月9日接到实际控制人" +
//		 "江苏宏宝集团有限公司（以下简称“宏宝集团”）通知，" +
//		 "宏宝集团将其所持本公司无限售条件流通股份500万股（占公司总股本的2．72％）质押给" + "华夏银行股份有限公司苏州分行，为"
//		 + "张家港市宏大钢管有限公司向华夏银行股份有限公司苏州分行" +
//		 "申请最高融资额提供担保，股权质押登记日为2012年11月8日，质押期限至2013年11月5日止；同日，" + "宏宝集团" +
//		 "将其所持本公司无限售条件流通股份1000万股（占公司总股本的5．43％）质押给"
//		 + "江苏张家港农村商业银行股份有限公司，为张家港保税区" + "康龙国际贸易有限公司向"
//		 +
//		 "江苏张家港农村商业银行股份有限公司申请的流动资金贷款提供担保，股权质押登记日为2012年11月8日，质押期限至2014年11月5日止。上述质押登记手续已在中国证券登记结算有限责任公司深圳分公司办理完毕。";
//		 content = "微博是最新的交流方式新浪微博,微博很好微博很大微博!";

//		content = "企业为了刻意凸显自身的先进性和本就薄弱的领导力，总会打出诸如颠覆、革命等旗号，以集聚人气和关注。而这一切对万达来说，只是浮云。在当前电子商务营商环境日趋成熟，网民习惯逐渐形成的大环境下，万达电商可以高薪挖来成熟市场内的人才搭建技术平台，也不必费尽心机去络线下资源，聚合及管理供应链，甚至在自身强大的线下门店配合下，也能很轻松地越过支付和配送的壁垒，扮演电子商务";
//		content = "【长乐一老板嫁女嫁妆2.1亿 席开300多桌】近日，网友微博爆料“长乐一企业家嫁女，嫁妆高达2.1亿元”。记者18日核实了此事，长乐金峰镇金峰村一李姓企业家16日嫁女，嫁妆为2.1亿元的“创业基金”。据参加婚礼的村民介绍，婚宴摆了300多桌，场面相当豪华。福州日报(图：@木板儿)";
		 
		Configuration job = new Configuration();
		job.set("fs.default.name", "hdfs://192.168.1.67:9000");
			
		FileSystem fs = FileSystem.get(job);
		
		FileSplit split = new FileSplit(new Path("/project/895/2008/07/20080725"),0,0,new String[]{});
		Reporter reporter = null;
		TensynRecordReader recorderReader = new TensynRecordReader(job, split, reporter);
		Text key = recorderReader.createKey();
		Text value = recorderReader.createValue();
		long count=0;
		long startTime = System.currentTimeMillis();
		
		BufferedWriter out = new BufferedWriter(new FileWriter("/home/qiuqiang/newword.txt"));
		JSONParser parser = new JSONParser();
		JSONObject content = null;
		while(recorderReader.next(key, value)){
			
			key = recorderReader.createKey();
			value = recorderReader.createValue();
			count ++;
			if(count%10000==0){
				System.out.println(count);
			}
			try {
				content = (JSONObject)parser.parse(value.toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				content = null;
			}
			
			if(content==null){
				continue;
			}
			LearnTool learn = new LearnTool();
			List<Term> paser = NlpAnalysis.parse(StringUtil.rmHtmlTag((String)content.get("content")), learn);
			Set<String> keySets = new HashSet<String>();
			for(Term term: paser){
				keySets.add(term.getName());
			}
			List<Entry<String, Double>> topTree = learn.getTopTree(1000);
			if(topTree!=null){
//				System.out.println(topTree);
				for(Entry<String, Double> entry: topTree){
					if(keySets.contains(entry.getKey())){
						out.write(entry.getKey()+"\t"+entry.getValue()+"\n");
					}
				}
				out.flush();
			}
		}
		
		out.close();
		
	}
}
