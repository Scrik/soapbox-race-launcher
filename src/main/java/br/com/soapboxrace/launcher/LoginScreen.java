package br.com.soapboxrace.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LoginScreen extends Shell {
	private Text txtPassword;
	private Text txtEmail;
	private Label lblStatus;

	private String userId;
	private String loginToken;
	private String serverURL;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			LoginScreen shell = new LoginScreen(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 */
	public LoginScreen(Display display) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.TITLE);
		setLayout(new FormLayout());
		setText("Soapbox-Hill | server launcher");
		setSize(450, 300);

		Menu menu = new Menu(this, SWT.BAR);
		setMenuBar(menu);

		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("Settings");

		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);

		MenuItem mntmServerSelect = new MenuItem(menu_1, SWT.NONE);
		mntmServerSelect.setText("Select a server...");

		MenuItem mntmAutoLogin = new MenuItem(menu_1, SWT.CHECK);
		mntmAutoLogin.setText("Auto-Login on start");

		MenuItem mntmAbout = new MenuItem(menu, SWT.NONE);
		mntmAbout.setText("About");

		Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_IN);
		FormData fd_label = new FormData();
		fd_label.bottom = new FormAttachment(100, -21);
		fd_label.left = new FormAttachment(0);
		fd_label.top = new FormAttachment(100, -23);
		fd_label.right = new FormAttachment(0, 444);
		label.setLayoutData(fd_label);

		lblStatus = new Label(this, SWT.NONE);
		FormData fd_lblStatus = new FormData();
		fd_lblStatus.right = new FormAttachment(label, 0, SWT.RIGHT);
		fd_lblStatus.top = new FormAttachment(label, 2);
		fd_lblStatus.left = new FormAttachment(0, 5);
		lblStatus.setLayoutData(fd_lblStatus);
		lblStatus.setText("Status: Idle");

		Composite composite = new Composite(this, SWT.NONE);
		composite.setLayout(null);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.left = new FormAttachment(label, 0, SWT.LEFT);
		fd_composite.bottom = new FormAttachment(0, 109);
		fd_composite.right = new FormAttachment(0, 260);
		composite.setLayoutData(fd_composite);

		Label lblEmail = new Label(composite, SWT.NONE);
		lblEmail.setBounds(22, 13, 35, 15);
		lblEmail.setText("Email: ");

		Label lblPassword = new Label(composite, SWT.NONE);
		lblPassword.setBounds(22, 40, 56, 15);
		lblPassword.setText("Password: ");

		txtEmail = new Text(composite, SWT.BORDER);
		txtEmail.setBounds(90, 10, 160, 21);
		txtEmail.setTextLimit(254);

		txtPassword = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setBounds(90, 37, 160, 21);
		txtPassword.setTextLimit(64);

		Button btnLogin = new Button(composite, SWT.NONE);
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				doLogin(txtEmail.getText(), txtPassword.getText());
			}
		});
		btnLogin.setBounds(194, 64, 56, 25);
		btnLogin.setText("Login");
	}

	private void doLogin(String email, String password) {
		try {
			DocumentBuilderFactory dcFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dcBuilder = dcFactory.newDocumentBuilder();

			String param = String.format("email=%s&password=%s",
					URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8.toString()),
					URLEncoder.encode(password, java.nio.charset.StandardCharsets.UTF_8.toString()));

			serverURL = "http://nfsw-server.duckdns.org:1337/nfsw/Engine.svc"; //because soapbox-hill isn't up yet.
			URL serverAuth = new URL(
					serverURL.concat("/User/AuthenticateUser?").concat(param));
			HttpURLConnection serverCon = (HttpURLConnection) serverAuth.openConnection();
			serverCon.setRequestMethod("GET");

			BufferedReader in = new BufferedReader(new InputStreamReader(serverCon.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			Document doc = dcBuilder.parse(new InputSource(new StringReader(response.toString())));

			if (serverCon.getResponseCode() != 200) {
				lblStatus.setText("Status: ".concat(doc.getElementsByTagName("Description").item(0).getTextContent()));
			} else {
				userId = doc.getElementsByTagName("UserId").item(0).getTextContent();
				loginToken = doc.getElementsByTagName("LoginToken").item(0).getTextContent();
				System.out.printf("User Id: %s\r\nToken: %s", userId, loginToken);
				lblStatus.setText("Status: Logged in!");
			}

		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
