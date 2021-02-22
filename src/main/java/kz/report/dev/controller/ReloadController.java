package kz.report.dev.controller;

import kz.report.dev.services.reload.Reload;
import kz.report.dev.services.reload.reloadimpl.ReloadFromDb;
import kz.report.dev.services.reload.reloadimpl.ReloadFromDbLast;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReloadController {
    @GetMapping("reload")
    public String reload(@RequestParam(name = "dbeg") String dbeg, @RequestParam(name = "dend") String dend) throws Exception {
//        Reload reloadFromSynergy = new ReloadFromDb(dbeg, dend);
//        reloadFromSynergy.reload();
        return "Сохранено";
    }

    @GetMapping("lastreload")
    public String lastReload(@RequestParam(name = "dbeg") String dbeg, @RequestParam(name = "dend") String dend) throws Exception {
//        Reload reload = new ReloadFromDbLast(dbeg, dend);
//        reload.reload();
        return "Ok";
    }
}
