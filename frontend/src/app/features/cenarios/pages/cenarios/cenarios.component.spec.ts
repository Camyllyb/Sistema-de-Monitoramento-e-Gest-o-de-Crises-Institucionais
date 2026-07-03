import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CenariosComponent } from './cenarios.component';

describe('CenariosComponent', () => {
  let component: CenariosComponent;
  let fixture: ComponentFixture<CenariosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CenariosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CenariosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
