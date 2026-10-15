package checkmate.insea.fishbio.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FishDataSyncScheduler {
    
    private final FishInfoService fishInfoService;
    
    // 매일 새벽 2시에 데이터 동기화 (선택사항)
    // @Scheduled(cron = "0 0 2 * * *")
    public void scheduledDataSync() {
        log.info("스케줄된 해양생물 데이터 동기화 시작");
        fishInfoService.syncAllMarineBioData();
        log.info("스케줄된 해양생물 데이터 동기화 완료");
    }
}
