package de.intelligence.drp.core.connection;

import java.util.List;

import de.intelligence.drp.core.Initializable;

public sealed interface IIPCConnection extends IConnection, Initializable permits IPCConnectionImpl {

    void setNamedPipes(List<String> pipes);

    List<String> getNamedPipes();

}
