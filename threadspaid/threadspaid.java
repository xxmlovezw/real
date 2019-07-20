import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.plaf.ActionMapUIResource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class threadspaid {

	static Map<String, String> map = new HashMap<String, String>(); 
	static final String url = "http://www.mm131.com/";

	public static String connectForHtml(String url) {

		String html = null;
		HttpURLConnection con = null;
		InputStream inputStream = null;
		URL mmUrl = null;
		int code;
		int x;
		byte b[] = new byte[1024];
		System.out.println(url);
		while (true) {
			try {
				mmUrl = new URL(url);
				con = (HttpURLConnection) mmUrl.openConnection();
				code = con.getResponseCode();
				inputStream = con.getInputStream();
				while ((x = inputStream.read(b)) != -1) {
					html += new String(b, "gbk");
				}
				break;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (inputStream != null)
						inputStream.close();
					if (con != null)
						con.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return html;
	}

	public static void ParseHeadHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements elementById = doc.getElementsByClass("nav");
		for (int i = 0; i < elementById.size(); i++) {
			org.jsoup.nodes.Element element = elementById.get(i);
			Elements allElements = element.getAllElements();
			for (int j = 0; j < allElements.size(); j++) {
				org.jsoup.nodes.Element element2 = allElements.get(j);
				String attr = element2.attr("href");
				String text = element2.ownText();
				if (!"".equals(text))
					map.put(text, attr);
			}

		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {

	
		new TheadDownload("https://www.mm131.net/qingchun/").DownloadPicture();
	
	}

}

class TheadDownload {
	private AtomicInteger integer = new AtomicInteger(0); 
	private Object obj = new Object(); 
	private Object obj1 = new Object();
	private String url; 
	private ExecutorService service; 
	final int total = 20;
	private ArrayBlockingQueue<String> srcs;
	private ArrayBlockingQueue<String> hrefs; 
	private ArrayBlockingQueue<String> nextHtml; 

	public TheadDownload(String url) {
		this.url = url;
		service = Executors.newFixedThreadPool(4);
		srcs = new ArrayBlockingQueue<String>(total);
		hrefs = new ArrayBlockingQueue<String>(total);
		nextHtml = new ArrayBlockingQueue<String>(1);
	}

	
	private String connectForHtml(String url) {
		String html = null;
		HttpURLConnection con = null;
		InputStream inputStream = null;
		URL mmUrl = null;
		int code;
		int x;
		byte b[] = new byte[1024];
		System.out.println(url);
		try {
			mmUrl = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				con = (HttpURLConnection) mmUrl.openConnection();
				code = con.getResponseCode();
				inputStream = con.getInputStream();
				while ((x = inputStream.read(b)) != -1) {
					html += new String(b, "gbk");
				}
				break;
			} catch (IOException e) {
				System.err.println("connect failed");
			} finally {

				try {
					if (inputStream != null)
						inputStream.close();
					if (con != null)
						con.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		return html;
	}

	
	private void ParsebodyHtml(String html) {
		Document doc = Jsoup.parse(html);
		Elements elementsByClass2 = doc.select("[width=120]");
		for (int i = 0; i < elementsByClass2.size(); i++) {
			org.jsoup.nodes.Element element = elementsByClass2.get(i);
			org.jsoup.nodes.Element parent = element.parent();
			try {
				String attr = parent.attr("href");
				hrefs.put(attr);
				System.err.println(attr);
				String attr2 = element.attr("src");
				srcs.put(attr2);
				System.err.println(attr2);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Elements select = doc.select("a.page-en");
		try {
			for (int i = 0; i < select.size(); i++) {
				Element element = select.get(i);
				String attr = element.attr("href");
				if (element.ownText().equals("next page")) {
					nextHtml.put(attr);
					System.out.println(attr);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private byte[] readInputStream(InputStream inStream) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
		} catch (Exception e) {

		} finally {
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outStream.toByteArray();
	}


	private void connectForLoad(String src, String url, String filepath, int tatol) {
		System.out.println(url);
		System.out.println(src);
		InputStream inputStream = null;
		FileOutputStream outStream = null;
		HttpURLConnection connection = null;
		URL srcUrl = null;
		File file1 = null;
		int code;
		int i;
		File file = new File("E:\\mm31\\" + filepath);
		if (!file.exists()) {
			file.mkdirs();
		}
		for (i = 1; i <= tatol;) {
			try {
				srcUrl = new URL(src);
				connection = (HttpURLConnection) srcUrl.openConnection();
				connection.addRequestProperty("referer", url);
				code = connection.getResponseCode();
				inputStream = connection.getInputStream();
				file1 = new File(file.getAbsolutePath() + "\\" + i + ".jpg");
				byte[] data = readInputStream(inputStream);
				outStream = new FileOutputStream(file1);
				outStream.write(data);
			} catch (Exception e) {

			} finally {
				try {
					if (outStream != null)
						outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					
				}
				if (i == 1) {
					url = url.substring(0, url.length() - 5) + "_" + (i + 1) + ".html";
					src = src.substring(0, src.length() - 5) + (i + 1) + ".jpg";

				} else {
					url = url.substring(0, url.indexOf("_") + 1) + (i + 1) + ".html";
					src = src.substring(0, src.lastIndexOf("/") + 1) + (i + 1) + ".jpg";
				}
				i++;
			}

		}
		
	}

	
	private void ParseForLoad(String html, String url, String src) throws Exception {
		String filePath = null;
		System.err.println("ParseForLoad");
		Document doc = Jsoup.parse(html);
		Elements select = doc.select("div.content-page");
		Elements element = select.select("span.page-ch");
		filePath = doc.select("h5").text();
		int total = Integer.parseInt(element.get(0).text().substring(1, 3));
		System.out.println("start download");
		connectForLoad(src, url, filePath, total);
	}

	
	public void DownloadPicture() {
		String connectForHtml = connectForHtml(this.url); 
		ParsebodyHtml(connectForHtml); 
		for (int i = 0; i < 4; i++) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					String href = null;
					String src = null;
					while (true) {
						try {
							synchronized (obj) {
								href = hrefs.take();
								src = srcs.take();
							}
						} catch (Exception e) {
							
						}

						try {
							String html = connectForHtml(href);
							ParseForLoad(html, href, src);
						} catch (Exception e1) {
							try {
								synchronized (obj) {
									hrefs.put(href);
									srcs.put(src);
								}
								System.err.println("thread dead");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

						}
					
						synchronized (obj1) {
							if (hrefs.size() == 0) {
								String next = null;
								try {
									next = nextHtml.take();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								next = url + next;
								ParsebodyHtml(connectForHtml(next));
								System.err.println("next page");
							}
						}

					}
				}
			});
		}
	
	}

}
