package de.intelligence.drp.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public interface Kernel32 extends StdCallLibrary {

    Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

    HANDLE CreateFileA(String lpFileName, int dwDesiredAccess, int dwShareMode, WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes, HANDLE hTemplateFile);

    boolean ReadFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead, WinBase.OVERLAPPED lpOverlapped);

    boolean WriteFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToWrite, IntByReference lpNumberOfBytesWritten, WinBase.OVERLAPPED lpOverlapped);

    boolean CloseHandle(HANDLE hObject);

    boolean PeekNamedPipe(HANDLE hNamedPipe, byte[] lpBuffer, int nBufferSize, IntByReference lpBytesRead,IntByReference lpTotalBytesAvail, IntByReference lpBytesLeftThisMessage);

    int GetLastError();

}
