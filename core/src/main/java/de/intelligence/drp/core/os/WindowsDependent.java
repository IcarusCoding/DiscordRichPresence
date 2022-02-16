package de.intelligence.drp.core.os;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public final class WindowsDependent implements OSDependent {

    private final Kernel32 kernel32;

    public WindowsDependent() {
        this.kernel32 = Native.load("kernel32", Kernel32.class);
    }

    public Kernel32 getKernel32() {
        return this.kernel32;
    }

    @Override
    public int getCurrentPID() {
        return this.kernel32.GetCurrentProcessId();
    }

    public interface Kernel32 extends StdCallLibrary {

        WinNT.HANDLE CreateFileA(String lpFileName, int dwDesiredAccess, int dwShareMode, WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes, WinNT.HANDLE hTemplateFile);

        boolean ReadFile(WinNT.HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead, WinBase.OVERLAPPED lpOverlapped);

        boolean WriteFile(WinNT.HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToWrite, IntByReference lpNumberOfBytesWritten, WinBase.OVERLAPPED lpOverlapped);

        boolean CloseHandle(WinNT.HANDLE hObject);

        boolean PeekNamedPipe(WinNT.HANDLE hNamedPipe, byte[] lpBuffer, int nBufferSize, IntByReference lpBytesRead, IntByReference lpTotalBytesAvail, IntByReference lpBytesLeftThisMessage);

        int GetLastError();

        int GetCurrentProcessId();

    }

}
