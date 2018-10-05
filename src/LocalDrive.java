import java.io.File;

@SuppressWarnings("serial")
public class LocalDrive extends File {
	public LocalDrive(File arg0) {
		super(arg0.getAbsolutePath());
	}	
	
	public LocalDrive(String arg0) {
		super(arg0);
	}

	boolean isSSD;
	
	public void setSSDStatus(boolean isSSD) {
		this.isSSD = isSSD;
	}
	
}
