# lecture_otel

앱이 기록을 만들고 → Collector가 모아서 → Tempo에 저장 → Grafana로 본다.

## 아키텍처

```mermaid
flowchart LR
    User(["🧑 사용자"])

    subgraph apps["① 기록을 만드는 쪽"]
        direction TB
        US["user-service<br/>:8081"]
        OS["order-service<br/>:8082"]
        PS["payment-service<br/>:8083"]
    end

    subgraph pipe["② 모으고 보여주는 쪽"]
        direction TB
        COL["otel-collector<br/>:4317 · :4318"]
        TEMPO["tempo<br/>:3200"]
        GRAF["grafana<br/>:3000"]
    end

    User -->|"HTTP"| US
    US -->|"HTTP"| OS
    OS -->|"HTTP"| PS

    US -.->|"trace"| COL
    OS -.->|"trace"| COL
    PS -.->|"trace"| COL

    COL --> TEMPO
    GRAF -->|"조회"| TEMPO
    User -->|"브라우저"| GRAF

    classDef app fill:#dbeafe,stroke:#3b82f6,color:#1e3a5f
    classDef obs fill:#dcfce7,stroke:#22c55e,color:#14532d
    class US,OS,PS app
    class COL,TEMPO,GRAF obs
```

**실선**은 실제 서비스 호출, **점선**은 트레이스 전송. 둘은 별개의 길이라 Collector가 죽어도 서비스는 돈다.

| 컨테이너 | 포트 | 역할 |
|---|---|---|
| user / order / payment | 8081 / 8082 / 8083 | 서로 체인으로 호출하는 실습용 서비스 |
| otel-collector | 4317(gRPC), 4318(HTTP), 8889 | 트레이스를 받아 묶어서 Tempo로 전달 |
| tempo | 3200 | 트레이스 저장소 |
| grafana | 3000 | 트레이스 조회 화면 (admin / admin) |

- Tempo의 4317은 호스트에 열지 않았다 — 트레이스는 **반드시 Collector를 거치게** 하려는 의도.
- Collector의 **8889는 Prometheus용 메트릭 포트인데, 지금은 열려만 있다.** compose에 Prometheus 컨테이너가 없고 `otel-collector-config.yaml`의 prometheus receiver도 주석 처리돼 있다. 앱도 `OTEL_METRICS_EXPORTER=none`이라 이번 실습은 **트레이스만** 다룬다.
- 서비스 빌드는 컨테이너 안에서 `./gradlew build -x test` 로 돌아간다. **로컬에 Gradle·JDK 없어도 되고 Docker만 있으면 된다.**

---

## 시퀀스 다이어그램

```mermaid
sequenceDiagram
    autonumber
    participant App as Application<br/>(내 서비스)
    participant Col as OTel Collector<br/>(수집기)
    participant Tempo as Tempo<br/>(저장소)
    participant Graf as Grafana<br/>(대시보드)
    actor User as 사용자

    rect rgba(90, 150, 255, 0.10)
    Note over App, Tempo: ① 쌓이는 길 — 요청이 들어올 때마다 자동으로

    App->>App: 요청을 처리하면서 기록을 남긴다 (span)
    App->>Col: 기록을 보낸다 (OTLP · :4317)
    Col->>Col: 잠깐 모았다가 정리한다
    Col->>Tempo: 정리한 기록을 넘긴다
    Tempo->>Tempo: 디스크에 저장한다
    end

    rect rgba(90, 200, 140, 0.10)
    Note over Tempo, User: ② 보는 길 — 궁금할 때만

    User->>Graf: "이 요청 왜 느렸지?" 검색
    Graf->>Tempo: 그 기록 찾아줘 (:3200)
    Tempo-->>Graf: 기록 전달
    Graf-->>User: 그림으로 보여준다
    end
```