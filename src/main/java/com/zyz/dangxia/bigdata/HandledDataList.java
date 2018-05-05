package com.zyz.dangxia.bigdata;

import com.zyz.dangxia.entity.HandledData;
import com.zyz.dangxia.repository.HandledDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class HandledDataList {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    //读写锁 适用于对数据结构读取的次数远远大于写的次数，不同线程读与读之间不会互斥，
    // 但是一旦一个线程加了写锁，其它线程无法读取知道写锁被释放
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    @Autowired
    private HandledDataRepository handledDataRepository;

    private List<HandledData> allDatas;

    private HandledDatasForOneClass[] datas;

    public List<HandledData> getAllDatas() {
        return allDatas;
    }

    public double[][] getDataWithDistanceList(int classId) {
        Lock rLock = lock.readLock();
        rLock.lock();
        try {
            double[][] source = datas[classId - 1].dataWithDistance;
            double[][] copy = new double[source.length][7];

            for (int i = 0; i < source.length; i++) {
                System.arraycopy(source[i], 0, copy[i], 0, source.length);
            }
            return copy;

        } finally {
            rLock.unlock();
        }
    }

    @PostConstruct
    @Scheduled(cron = "0 0 3 * * ?")
    public void init() {
        Lock wLock = lock.writeLock();
        wLock.lock();
        try {
            //逐个查出各大类的数据，进行赋值
            allDatas = handledDataRepository.findAll();
            datas = new HandledDatasForOneClass[13];
            for (int j = 0; j < 13; j++) {
                List<HandledData> temp = handledDataRepository.findByClassId(j + 1);
                datas[j] = new HandledDatasForOneClass(temp.size());
                for (int i = 0; i < temp.size(); i++) {
                    HandledData data = allDatas.get(i);
                    datas[j].dataWithDistance[i][0] = data.getC0();
                    datas[j].dataWithDistance[i][1] = data.getC1();
                    datas[j].dataWithDistance[i][2] = data.getC2();
                    datas[j].dataWithDistance[i][3] = data.getC3();
                    datas[j].dataWithDistance[i][4] = data.getT();
                    datas[j].dataWithDistance[i][5] = data.getP();
                    datas[j].dataWithDistance[i][6] = 0;//最后一个变量是距离
                }
                logger.info("{}分类的蝇量数据装填完毕，一共有{}条数据", j + 1, datas[j].dataWithDistance.length);
            }
            System.gc();
        } finally {
            wLock.unlock();
        }
    }

    //用于存储一个大分类下的样本数据
    class HandledDatasForOneClass {
        //由于样本的数量是不固定的，需要创建时决定数组大小
        double[][] dataWithDistance;

        HandledDatasForOneClass(int count) {
            dataWithDistance = new double[count][7];
        }
    }
}
