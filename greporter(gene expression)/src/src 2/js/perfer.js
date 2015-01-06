fs = require('fs');
dirpath='.\\';
//dirpath="";
function prefer()
{
    this.readperfer=readperfer;    
    this.prejson={};
    function readperfer()
    {
        fs.readFile(dirpath+'perfer.json',"utf-8", 
            function (err, data) 
            {
                if (!err)
                {
                    console.log(data);
                    prejson=eval("("+data+")");
                }
                else
                {
                    prejson={};
                    console.log("No perfer file ,will create one");
                }
            });
    }
    this.set=set;
    function set(proto,val)
    {
        prejson[proto]=val;
        fs.writeFile(dirpath+'perfer.json',JSON.stringify(prejson),"utf-8",function (err) {
          if (err) 
            throw err;
          console.log('It\'s saved!');
      });
    }
    this.get=get;
    function get(pro)
    {
        return prejson[pro];
    }
    this.readperfer();
    console.log("Set Preference Successful");
    return this;
}

function savepath()
{
    globpre.set("Pybinarypath",$("#Pybinarypath").val() );
    globpre.set("Rbinarypath",$("#Rbinarypath").val()   );
    globpre.set("Javabinarypath",$("#Javabinarypath").val() );
}
globpre=prefer();