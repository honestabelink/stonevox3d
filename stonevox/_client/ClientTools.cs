using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

public class ClientTools
{
    public Dictionary<string, ITool> _tools = new Dictionary<string, ITool>();

    private ITool activetool;
    private ITool lastactivetool;

    public ClientTools()
    {
        _tools.Add("add", new AddTool());
        _tools.Add("recolor", new RecolorTool());
        _tools.Add("remove", new RemoveTool());
        _tools.Add("move", new MoveTool());

        string first = "remove";
        lastactivetool = _tools[first];
        activetool = _tools[first];
    }

    public void use()
    {
        activetool.use();
    }

    public bool onselectionchanged(ClientInput input, QbMatrix matrix, RaycastHit hit)
    {
        return activetool.onraycasthitchanged(input, matrix,hit);
    }
}
